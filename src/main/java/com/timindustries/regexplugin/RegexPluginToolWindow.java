package com.timindustries.regexplugin;

import javax.swing.*;

import com.intellij.openapi.wm.ToolWindow;
import com.timindustries.regexplugin.uiInterface.ComponentFactory;

public class RegexPluginToolWindow {

  private RegexPanel fPanel;

  private RegexPluginConfig fConfig = new RegexPluginConfig();

  public RegexPluginToolWindow(ToolWindow toolWindow) throws Exception {
    fPanel = new RegexPanel(fConfig, ComponentFactory.getInstance());
  }

  public JPanel getContent() {
      return fPanel;
    }
}
