package com.timindustries.regexplugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.timindustries.regexplugin.actions.GenericAction;

public class MatchAction extends GenericAction {

  private final ArrayList<Match> foundMatches;

  private int flags = 0;

  private boolean replaceAll = true;

  private final RegexPanel regexPanel;

  private Pattern pattern = null;

  public MatchAction(final RegexPanel regexPanel, final IconCache icons) {
    super(regexPanel, "find", icons.getIcon("find.png"));
    this.regexPanel = regexPanel;
    this.foundMatches = new ArrayList<Match>();
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
      if (isReplaceAll()) {
        res = matcher.replaceAll(
            regexPanel.getReplaceInputComponent().getText());
      } else {
        res = matcher.replaceFirst(
            regexPanel.getReplaceInputComponent().getText());
      }
      regexPanel.getReplaceOutputComponent().setText(res);

    } catch (Exception e) {
      makeErrorOutput(e);
    }
  }

  private Pattern getPattern(RegexPanel regexPanel) {
    String patternString = regexPanel.getPatternComponent().getText();
    if ((pattern == null) || (pattern.flags() != flags) ||
        (!pattern.pattern().equals(patternString))) {
      pattern = Pattern.compile(regexPanel.getPatternComponent().getText(),
          flags);
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
    flags |= flag;
  }

  private void removeFlag(final int flag) {
    flags &= (~flag);
  }

  public void setFlag(final int flag, boolean enable) {
    if (enable) {
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
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);

      switch (c) {
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

    final StringBuilder out = new StringBuilder("<html><body style=\"font-size:24px\">");

    int idx = 0;
    final Iterator<Match> i = foundMatches.iterator();
    int matchIdx = 0;
    while (i.hasNext()) {
      final Match match =  i.next();
      final String beforeMatch = text.substring(idx, match.getStartIdx());
      out.append(convertToHTML(beforeMatch));

      final String allTextBeforeMatch = text.substring(0, match.getStartIdx());

      String totalLine = match.groups.get(0).text;
      var decal = -1*allTextBeforeMatch.length();
      for(int j = 1; j < match.groups.size(); j++){
          String fragment = "".concat("<span style=\"background-color:").concat(randomRgba())
                .concat("\">")
                .concat(convertToHTML(match.groups.get(j).text))
                .concat("</span>");
        totalLine = totalLine.substring(0, calculateStartFragmentIndexOffset(match, j, decal))
                .concat(fragment)
                .concat(totalLine.substring(calculateEndFragmentIndexOffset(match, decal, j)));
        decal += fragment.length() - match.groups.get(j).text.length();
      }

      out.append("<a href=\"http://")
              .append(matchIdx)
              .append("\"><span style=\"background-color:")
              .append("rgba(0, 255," + (matchIdx % 2 == 0 ? "0" : "145") + ",0.2)")
              .append("\">")
              .append(totalLine)
              .append("</span></a>");

      idx = match.getEndIdx();
      matchIdx++;
    }
    if (idx < text.length()) {
      out.append(convertToHTML(text.substring(idx)));
    }
    out.append("</body></html>");

    regexPanel.getFindOutputComponent().setText(out.toString());
  }

  private static int calculateEndFragmentIndexOffset(Match match, int decal, int j) {
    return match.groups.get(j).endIdx + decal;
  }

  private static int calculateStartFragmentIndexOffset(Match match, int j, int decal) {
    return match.groups.get(j).startIdx + decal;
  }

  private String colorToHex(Color color) {
    return String.format("#%02x%02x%02x30", color.getRed(), color.getGreen(), color.getBlue());
  }

  private String randomRgba(){
    return String.format("rgba(%d, %d, %d, 0.6)", (int)(Math.random()*1000)%255, (int)(Math.random()*1000)%200,(int)(Math.random()*1000)%255);
  }

  public void setReplaceAll(final boolean b) {
    this.replaceAll = b;
  }

  public ArrayList<Match> getFoundMatches() {
    return foundMatches;
  }

  public boolean isSelected(int flag) {
    return (this.flags & flag) != 0;
  }

  public boolean isReplaceAll() {
    return replaceAll;
  }
}
