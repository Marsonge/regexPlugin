package regexPlugin.actions;

import regexPlugin.IconCache;
import regexPlugin.MatchAction;
import regexPlugin.RegexPluginConfig;

public class ToggleFlagAction extends MatchToggleAction {
  private final int flag;

  public ToggleFlagAction(final String key, final String iconResource, final int flag, final MatchAction action, IconCache iconCache, RegexPluginConfig config) {
    super(null, key, iconCache.getIcon(iconResource), action, config);
    this.flag = flag;
  }

  public boolean isSelected() {
    return fAction.isSelected(flag);
  }

  public void setSelected(boolean b) {
    fAction.setFlag(flag, b);
    perform();
  }

  public boolean showDescription() {
    return fConfig.showLabels;
  }

}
