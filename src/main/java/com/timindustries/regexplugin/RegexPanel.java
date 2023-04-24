package com.timindustries.regexplugin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.timindustries.regexplugin.library.LibraryComboBoxModel;
import com.timindustries.regexplugin.regexEditor.RegexEditor;
import com.timindustries.regexplugin.ui.LabelEx;
import com.timindustries.regexplugin.uiInterface.ComponentManager;
import com.timindustries.regexplugin.actions.DeleteCurrentLibraryEntryAction;
import com.timindustries.regexplugin.actions.GenericAction;
import com.timindustries.regexplugin.actions.GenericToggleAction;
import com.timindustries.regexplugin.actions.NewLibraryEntryAction;
import com.timindustries.regexplugin.actions.ReplaceAllAction;
import com.timindustries.regexplugin.actions.ToggleAutoUpdateAction;
import com.timindustries.regexplugin.actions.ToggleFlagAction;
import com.timindustries.regexplugin.event.EventManager;
import com.timindustries.regexplugin.event.ObjectEvent;
import com.timindustries.regexplugin.event.ObjectListener;
import com.timindustries.regexplugin.ui.Resources;

public class RegexPanel extends JPanel {

  private JTextComponent pattern;

  private JTextComponent text;

  private JTextComponent matchDetails;

  private JTextComponent replaceOutput;

  private JTextComponent replaceInput;

  private JTextComponent findOutput;

  private final MatchAction fMatchAction;

  private final JSplitPane upperPane;

  private final JSplitPane lowerPane;

  private final JSplitPane leftPane;

  private final JSplitPane replacePane;

  private final JSplitPane midPane;

  private final ComponentManager uiFactory;

  private final LibraryManager libraryManager = new LibraryManager();

  private final LibraryComboBoxModel libraryModel = new LibraryComboBoxModel();

  private final HashMap m_flags = new HashMap();

  private ObjectListener eventHandler = null;

  private final RegexPluginConfig config;

  private final IconCache iconCache = new IconCache();

  private final ToggleSplitPane fTogglePane;

  private final JComponent fToolbar;

  public static final String EVENT_SHOW_LABELS = "showLabels";

  public RegexPanel(final RegexPluginConfig config,
                    final ComponentManager factory) throws Exception {
    this.config = config;
    libraryManager.init(this);
    uiFactory = factory;

    // create components
    setLayout(new BorderLayout());

    fMatchAction = new MatchAction(this, iconCache);
    final AutoUpdateDocumentListener
        autoUpdater =
        new AutoUpdateDocumentListener(
            fMatchAction, config);

    final JScrollPane patternScrollPane = createPatternScrollPane(autoUpdater);
    final JScrollPane textScrollPane = createTextScrollPane(autoUpdater);
    upperPane = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, patternScrollPane,
                                textScrollPane);

    final JScrollPane scrolledReplaceInput = createReplaceInputScrollPane(
        autoUpdater);
    final JScrollPane scrolledReplaceOutput = createReplaceOutputScrollPane();
    replacePane = createSplitPane(JSplitPane.VERTICAL_SPLIT, scrolledReplaceInput,
                                  scrolledReplaceOutput);

    final JScrollPane scrolledFindOutput = createFindOutputScrollPane();
    final JScrollPane scrolledMatchDetails = createMatchDetailsScrollPane();

    lowerPane = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrolledFindOutput,
                                scrolledMatchDetails);

    leftPane = createSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane, lowerPane);
    midPane = createSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, replacePane);

    fTogglePane = new ToggleSplitPane(midPane, createReferenceScrollPane(),
                                                               iconCache);

    config.initializeSize(getSize(), false);

    setDividerPositions(config);

    fToolbar = createToolbar();
    add(fToolbar, BorderLayout.PAGE_START);
    add(fTogglePane, BorderLayout.CENTER);

    eventHandler = new ObjectListener() {
      public void event(final ObjectEvent event) {
        if (event.getSource() == EventManager.getGlobalTarget()) {
          if (LibraryManager.EVENT_DELETE.equals(event.getMessage())) {
            libraryManager.deleted((String) event.getBody());
          } else if (LibraryManager.EVENT_IMPORT.equals(event.getMessage())) {
            libraryManager.imported();
          } else if (LibraryManager.EVENT_NEW.equals(event.getMessage())) {
            libraryManager.newed();
          }
        } else if (event.getSource() != libraryManager) {
          if (LibraryManager.EVENT_SAVE.equals(event.getMessage())) {
            if (libraryManager.getCurrentLibraryName()
                .equals(event.getBody())) {
              libraryManager.loadLibrary((String) event.getBody());
            }
          }
        }

        if (event.getSource() == EventManager.getGlobalTarget()) {
          if (RegexPanel.EVENT_SHOW_LABELS.equals(event.getMessage())) {
            if (getConfig().showLabels !=
                (Boolean) event.getBody()) {
              getConfig().showLabels =
                  (Boolean) event.getBody();
              invalidateToolbar();
              // take this out one day if we work out how.
              JOptionPane.showMessageDialog(RegexPanel.this,
                                            Resources.getInstance().getString(
                                                "showLabels.restartNeeded"));
            }
          }
        }
      }
    };

    EventManager.getInstance()
        .addObjectListener(libraryManager.getClass(), eventHandler);
    EventManager.getInstance()
        .addObjectListener(EventManager.getGlobalTarget(), eventHandler);
  }

  private void invalidateToolbar() {
    fToolbar.invalidate();
  }

  public void disposeComponent() {
    EventManager.getInstance()
        .removeObjectListener(libraryManager.getClass(), eventHandler);
    EventManager.getInstance()
        .removeObjectListener(EventManager.getGlobalTarget(), eventHandler);
  }

  public void saveLibrary() {
    libraryManager.saveCurrent();
  }

  private JScrollPane createReferenceScrollPane() throws Exception {
    final JScrollPane scrolledReference =
        uiFactory.getScrollPane(createReferencePane());
    scrolledReference.setBorder(
        BorderFactory.createTitledBorder(Resources.getTitle("reference")));
    return scrolledReference;
  }

  private JScrollPane createMatchDetailsScrollPane() {
    matchDetails = new JTextArea(5, 5);

    final JScrollPane scrolledMatchDetails = uiFactory.getScrollPane(matchDetails);
    matchDetails.setBackground(uiFactory.getDefaultBackgroundColor());
    scrolledMatchDetails.setBorder(
        BorderFactory.createTitledBorder(Resources.getTitle("matchDetails")));
    return scrolledMatchDetails;
  }

  private JScrollPane createFindOutputScrollPane() {
    findOutput = createHtmlOutputPane();
    findOutput.setBackground(uiFactory.getDefaultBackgroundColor());
    final JScrollPane scrolledFindOutput = uiFactory.getScrollPane(findOutput);


    scrolledFindOutput.setBorder(
        BorderFactory.createTitledBorder(Resources.getTitle("findOutput")));
    return scrolledFindOutput;
  }

  private JScrollPane createReplaceOutputScrollPane() {
    replaceOutput = new JTextPane();
    replaceOutput.setEditable(false);
    replaceOutput.setFont(uiFactory.getDefaultFont());
    replaceOutput.setBackground(uiFactory.getDefaultBackgroundColor());
    final JScrollPane scrolledReplaceOutput =
        uiFactory.getScrollPane(replaceOutput);
    scrolledReplaceOutput.setBorder(
        BorderFactory.createTitledBorder(Resources.getTitle("replaceOutput")));
    return scrolledReplaceOutput;
  }

  private JScrollPane createReplaceInputScrollPane(
      final AutoUpdateDocumentListener autoUpdater) {
    replaceInput = new JTextArea(5, 5);
    installUndoStuff(replaceInput);

    replaceInput.getDocument().addDocumentListener(autoUpdater);
    replaceInput.setFont(uiFactory.getDefaultFont());
    replaceInput.setBackground(uiFactory.getDefaultBackgroundColor());

    final JScrollPane scrolledReplaceInput = uiFactory.getScrollPane(replaceInput);
    scrolledReplaceInput.setBorder(
        BorderFactory.createTitledBorder(Resources.getTitle("replace")));
    return scrolledReplaceInput;
  }

  private JScrollPane createTextScrollPane(
      final AutoUpdateDocumentListener autoUpdater) {
    text = new JTextArea(5, 5);
    installUndoStuff(text);

    text.getDocument().addDocumentListener(autoUpdater);
    text.setFont(uiFactory.getDefaultFont());
    text.setBackground(uiFactory.getDefaultBackgroundColor());

    final JScrollPane textScrollPane = uiFactory.getScrollPane(text);
    textScrollPane.setBorder(
        BorderFactory.createTitledBorder(Resources.getTitle("text")));
    return textScrollPane;
  }

  private JScrollPane createPatternScrollPane(
      final AutoUpdateDocumentListener autoUpdater) {
    pattern = new RegexEditor(fMatchAction);
    installUndoStuff(pattern);

    pattern.getDocument().addDocumentListener(autoUpdater);
    pattern.setFont(uiFactory.getDefaultFont());
    pattern.setBackground(uiFactory.getDefaultBackgroundColor());

    final JScrollPane patternScrollPane = uiFactory.getScrollPane(pattern);
    patternScrollPane.setBorder(
        BorderFactory.createTitledBorder(Resources.getTitle("pattern")));
    return patternScrollPane;
  }

  private void installUndoStuff(JTextComponent text) {
    final UndoManager undo = new UndoManager();
    final Document doc = text.getDocument();
    doc.addUndoableEditListener(undo);
    text.getActionMap().put("undo", new AbstractAction("undo") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("undo " + undo.canUndo());
        if (undo.canUndo()) {
          undo.undo();
        }
      }
    });

    text.getActionMap().put("redo", new AbstractAction("redo") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("redo " + undo.canRedo());
        if (undo.canRedo()) {
          undo.redo();
        }
      }
    });

    text.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "undo");
    text.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "redo");
  }

  public int getFlags() {
    return fMatchAction.getFlags();
  }

  public void setFlags(final int flags) {
    final Collection keys = m_flags.keySet();
    for (Object key : keys) {
      final JCheckBox checkBox = (JCheckBox) key;
      final int flag = (Integer) m_flags.get(checkBox);
      checkBox.setSelected((flags & flag) != 0);
    }
    fMatchAction.setFlags(flags);
  }

  Object tAction(GenericToggleAction action) {
    return uiFactory.createToggleAction(action);
  }

  private void addToggleFlagAction(Object g, String name, String res, int flag) {
    addToggleAction(g,
                    new ToggleFlagAction(name, res, flag, fMatchAction, iconCache,
                                         config));
  }

  private void addAction(Object g, GenericAction action) {
    uiFactory.addToGroup(g, uiFactory.createGenericActionAdapter(action));
  }

  private JComponent createToolbar() {

    Object group = new DefaultActionGroup("regexactiongroup", false);
    uiFactory.addToGroup(group, uiFactory.createMenuAction(this, iconCache.getIcon(
        "menu.png"), uiFactory.createRegexPopupMenu(this)));
    addToggleAction(group,
                    new ToggleAutoUpdateAction(fMatchAction, iconCache, config));
    addToggleFlagAction(group, "comments", "comment.png", Pattern.COMMENTS);
    addToggleFlagAction(group, "caseInsensitive", "casesensitiv.png",
                        Pattern.CASE_INSENSITIVE);
    addToggleFlagAction(group, "multiline", "multiline.png", Pattern.MULTILINE);
    addToggleFlagAction(group, "dotall", "dotall.png", Pattern.DOTALL);
//    addToggleFlagAction(group, "unicodeCase", "dotall.png", Pattern.UNICODE_CASE);
//    addToggleFlagAction(group, "cannonEq", "dotall.png", Pattern.CANON_EQ);
    addToggleAction(group, new ReplaceAllAction(fMatchAction, iconCache, config));
    addToggleAction(group, fTogglePane.getToggleAction());
    addAction(group, fMatchAction);
    addAction(group, new NewLibraryEntryAction(this));
    addAction(group, new DeleteCurrentLibraryEntryAction(this));

    JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    JComponent toolbar = uiFactory.getComponent("regextoolbar", group, true);

    final JPanel libraryPanel = new JPanel(
        new FlowLayout(FlowLayout.LEFT, 5, 10));

    libraryManager.loadModel(libraryModel);
    final JComboBox libraryList = new JComboBox(libraryModel);
    libraryList.setEditable(true);
    libraryList.setPreferredSize(new Dimension(220, 22));
    libraryList.addActionListener(libraryManager);
    libraryList.addPopupMenuListener(libraryManager);
    libraryPanel.add(libraryList);
    wrapper.add(toolbar);
    wrapper.add(createLabel("library"));
    wrapper.add(libraryPanel);
    libraryManager.loadCurrent();
    libraryList.setSelectedItem(libraryManager.getCurrentLibraryName());

    return wrapper;
  }

  private void addToggleAction(Object group, GenericToggleAction action) {
    uiFactory.addToGroup(group, tAction(action));
  }

  private JLabel createLabel(final String resourceKey) {
    final JLabel label = new LabelEx(resourceKey);
    label.setFont(UIManager.getFont("TitledBorder.font"));
    label.setForeground(UIManager.getColor("TitledBorder.titleColor"));
    return label;
  }

  private JSplitPane createSplitPane(final int dir, final JComponent first,
                                     final JComponent second) {
    final JSplitPane pane = new JSplitPane(dir, true, first, second);
    pane.setBorder(null);
    // set this to 8, the default is far too large
    pane.setDividerSize(8);
    pane.setOneTouchExpandable(true);
    return pane;
  }

  private JTextPane createHtmlOutputPane() {
    final JTextPane res = new JTextPane();
    var font = uiFactory.getDefaultFont().deriveFont(36f);
    res.setFont(font);
    res.setEditable(false);
    res.setContentType("text/html");
    res.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(final HyperlinkEvent hyperlinkEvent) {
        if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          final int matchIdx = Integer.parseInt(
              hyperlinkEvent.getURL().getHost());
          showMatchDetails(
              (Match) fMatchAction.getFoundMatches().get(matchIdx));
        }
      }
    });
    return res;
  }

  private JTextPane createHtmlPaneForReference() {
    final JTextPane res = new JTextPane();
    var font = uiFactory.getDefaultFont();
    res.setFont(font);
    res.setEditable(false);
    res.setContentType("text/html");
    return res;
  }


  private JTextComponent createReferencePane() {
    var reference = createHtmlPaneForReference();
    reference.setEditable(false);
    try {
      final URL url = getClass().getResource("/help/reference.html");
      reference.setPage(url);
    } catch (Exception e) {
      Utils.handleException("error.panelStartup", e);
    }
    return reference;
  }

  private void showMatchDetails(final Match match) {
    final StringBuffer res = new StringBuffer();

    final Iterator i = match.getGroups().iterator();
    int idx = 0;
    while (i.hasNext()) {
      final Group group = (Group) i.next();
      res.append("Group[").append(idx++).append("] \"");
      res.append(group.getText()).append("\" at ");
      res.append(group.getStartIdx()).append(" - ");
      res.append(group.getEndIdx());
      res.append("\n");
    }

    matchDetails.setText(res.toString());
  }

  public JTextComponent getPatternComponent() {
    return pattern;
  }

  public JTextComponent getTextComponent() {
    return text;
  }

  public JTextComponent getMatchDetailsComponent() {
    return matchDetails;
  }

  public JTextComponent getReplaceOutputComponent() {
    return replaceOutput;
  }

  public JTextComponent getReplaceInputComponent() {
    return replaceInput;
  }

  public JTextComponent getFindOutputComponent() {
    return findOutput;
  }

  public LibraryComboBoxModel getLibraryModel() {
    return libraryModel;
  }

  public MatchAction getMatchAction() {
    return fMatchAction;
  }

  public String getCurrentLibraryName() {
    return libraryManager.getCurrentLibraryName();
  }

  public IconCache getIconCache() {
    return iconCache;
  }

  public void clearFields() {
    getPatternComponent().setText("");
    getTextComponent().setText("");
    getReplaceInputComponent().setText("");
    getReplaceOutputComponent().setText("");
    getMatchDetailsComponent().setText("");
    getFindOutputComponent().setText("");
  }

  public RegexPluginConfig getConfig() {
    config.update(upperPane.getDividerLocation(), lowerPane.getDividerLocation(),
                  leftPane.getDividerLocation(), midPane.getDividerLocation(),
                  replacePane.getDividerLocation(), config.autoUpdate,
                  fTogglePane.isOptionalOn(), fTogglePane.getDividerPos(),
                  config.showLabels);
    return config;
  }

  private void setDividerPositions(final RegexPluginConfig config) {
    fTogglePane.setOptional(config.referenceOn);

    upperPane.setDividerLocation(config.pos1);
    lowerPane.setDividerLocation(config.pos2);
    leftPane.setDividerLocation(config.pos3);
    midPane.setDividerLocation(config.pos4);
    replacePane.setDividerLocation(config.pos5);

    fTogglePane.setDividerLocation(config.referencePos);
  }

  public void doLayout() {
    initializeDividerPositions(false);
    super.doLayout();
  }

  public void initializeDividerPositions(boolean force) {
    if (config.initializeSize(getSize(), force)) {
      setDividerPositions(config);
    }
  }
}
