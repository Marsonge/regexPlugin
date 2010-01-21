package regexPlugin;

import regexPlugin.actions.GenericToggleAction;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;

public class ToggleSplitPane extends JPanel {
  private JComponent fFirst;
  private JComponent fOptional;
  private boolean fOptionalOn = false;
  private JSplitPane fSplitter;
  private GenericToggleAction fToggleAction;

  public ToggleSplitPane(JComponent first, JComponent optional, IconCache iconCache) {
    fFirst = first;
    fOptional = optional;
    fToggleAction = new GenericToggleAction(null,"referenceOptional", iconCache.getIcon("reference.png")) {
      public boolean isSelected() {
        return fOptionalOn;
      }

      public void setSelected(boolean b) {
        setOptional(b);
      }

      public void perform() {
        //not used
      }
    };
    setLayout(new BorderLayout());
    initChildren();
  }

  private void initChildren() {
    removeAll();

    if (fOptionalOn) {
      fSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, fFirst, fOptional);
      fSplitter.setBorder(null);
      // set this to 8, the default is far too large
      fSplitter.setDividerSize(8);
      fSplitter.setOneTouchExpandable(true);

      add(fSplitter);
    } else {
      add(fFirst);
    }
    validate();
  }

  public GenericToggleAction getToggleAction() {
    return fToggleAction;
  }

  public int getDividerPos() {
    if (fSplitter == null) {
      return 0;
    } else {
      return fSplitter.getDividerLocation();
    }
  }

  public void setOptional(boolean referenceOn) {
    fOptionalOn = referenceOn;
    initChildren();
  }

  public void setDividerLocation(int referencePos) {
    if (fSplitter != null) {
      fSplitter.setDividerLocation(referencePos);
    }
  }

  public boolean isOptionalOn() {
    return fOptionalOn;
  }
}
