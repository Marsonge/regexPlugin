package com.timindustries.regexplugin.actions;

import com.timindustries.regexplugin.IconCache;
import com.timindustries.regexplugin.MatchAction;
import com.timindustries.regexplugin.RegexPluginConfig;

public class ReplaceAllAction extends MatchToggleAction {

  public ReplaceAllAction(MatchAction action, IconCache icons,
      RegexPluginConfig config) {
    super(null, "replaceAll", icons.getIcon("replaceall.png"), action, config);
  }

  public boolean isSelected() {
    return fAction.isReplaceAll();
  }

  public void setSelected(boolean b) {
    fAction.setReplaceAll(b);
    perform();
  }

  public boolean showDescription() {
    return fConfig.showLabels;
  }

}
