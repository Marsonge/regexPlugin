package regexPlugin;

import regexPlugin.actions.GenericAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchAction extends GenericAction {

  private ArrayList foundMatches;

  private int flags = 0;

  private boolean replaceAll = true;

  private RegexPanel regexPanel;

  private Pattern pattern = null;

  public MatchAction(final RegexPanel regexPanel, final IconCache icons) {
    super(regexPanel, "find", icons.getIcon("find.png"));
    this.regexPanel = regexPanel;
    this.foundMatches = new ArrayList();
  }

  public void perform() {
    try {
      if (regexPanel.getPatternComponent().getText().length() == 0) {
        return;
      }
      final String textString = regexPanel.getTextComponent().getText();

      final Pattern thePattern = getPattern(regexPanel);

      final Matcher matcher = thePattern.matcher(textString);

      regexPanel.getMatchDetailsComponent().setText("");
      makeHtmlOutput(textString, matcher);

      String res = null;
      if (isReplaceAll() == true) {
        res = matcher.replaceAll(regexPanel.getReplaceInputComponent().getText());
      } else {
        res = matcher.replaceFirst(regexPanel.getReplaceInputComponent().getText());
      }
      regexPanel.getReplaceOutputComponent().setText(res);

    } catch (Exception e) {
      makeErrorOutput(e);
    }
  }

  private Pattern getPattern(RegexPanel regexPanel) {
    String patternString = regexPanel.getPatternComponent().getText();
    if ((pattern == null) ||
        (pattern.flags() != flags) ||
        (pattern.pattern().equals(patternString) == false)) {
      pattern = Pattern.compile(regexPanel.getPatternComponent().getText(), flags);
    }
    return pattern;
  }

  public int getFlags() {
    return flags;
  }

  public void setFlags(final int flags) {
    this.flags = flags;
  }

  private void addFlag(final int flag) {
    flags = flags | flag;
  }

  private void removeFlag(final int flag) {
    flags = flags & (~flag);
  }

  public void setFlag(final int flag, boolean enable) {
    if (enable == true) {
      addFlag(flag);
    } else {
      removeFlag(flag);
    }
  }


  private void makeErrorOutput(final Exception e) {
    final StringBuffer out = new StringBuffer("<html><body>");
    out.append("<font color=\"#FF0000\">");
    out.append(e.getMessage()).append("</font>");
    out.append("</body></html>");

    regexPanel.getFindOutputComponent().setText(out.toString());
  }

  private String convertToHTML(final String s) {
    final StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);

      switch (c){
        case ' ':
          sb.append("&nbsp;");
          break;
        case '\n':
          sb.append("<br>");
          break;
        case '<':
          sb.append("&lt;");
          break;
        case '>':
          sb.append("&gt;");
          break;
        case '&':
          sb.append("&amp;");
          break;
        case '"':
          sb.append("&quot;");
          break;
        default:
          sb.append(c);
          break;
      }
    }
    return sb.toString();
  }

  private void makeHtmlOutput(final String text, final Matcher matcher) {
    foundMatches.clear();
    while (matcher.find()) {
      final Match newMatch = new Match(matcher);
      if (newMatch.isRealMatch()) {
        foundMatches.add(newMatch);
      }
    }

    final StringBuffer out = new StringBuffer("<html><body>");

    int idx = 0;
    final Iterator i = foundMatches.iterator();
    int matchIdx = 0;
    while (i.hasNext()) {
      final Match match = (Match) i.next();
      final String beforeMatch = text.substring(idx, match.getStartIdx());
      out.append(convertToHTML(beforeMatch));
      String color = "#0000ff";
      if ((matchIdx % 2) != 0) {
        color = "#009900";
      }
      out.append("<a href=\"http://" + matchIdx++ +
                 "\"><font color=\"" + color + "\">" +
                 convertToHTML(match.getText()) + "</font></a>");
      idx = match.getEndIdx();
    }
    if (idx < text.length()) {
      out.append(convertToHTML(text.substring(idx)));
    }
    out.append("</body></html>");

    regexPanel.getFindOutputComponent().setText(out.toString());
  }

  public void setReplaceAll(final boolean b) {
    this.replaceAll = b;
  }

  public ArrayList getFoundMatches() {
    return foundMatches;
  }

  public boolean isSelected(int flag) {
    return (this.flags & flag) != 0;
  }

  public boolean isReplaceAll() {
    return replaceAll;
  }
}