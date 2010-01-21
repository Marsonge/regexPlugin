package regexPlugin.actions;

import regexPlugin.MatchAction;
import regexPlugin.RegexPluginConfig;
import regexPlugin.RegexPanel;

import javax.swing.Icon;

public abstract class MatchToggleAction extends GenericToggleAction {
  protected MatchAction fAction;
  protected RegexPluginConfig fConfig;

  public MatchToggleAction(final RegexPanel panel, final String key, final Icon icon, MatchAction action, final RegexPluginConfig config) {
    super(panel,key, icon);
    fAction = action;
    fConfig = config;
  }

  public void fireAction(boolean force) {
    if (force == true) {
      fAction.perform();
    }
  }

  public void perform() {
    fireAction(fConfig.isAutoUpdateEnabled());
  }

}