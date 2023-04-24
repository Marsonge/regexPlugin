package com.timindustries.regexplugin;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class RegexPluginToolWindowFactory implements ToolWindowFactory, DumbAware {

  /**
   * Create the tool window content.
   *
   * @param project    current project
   * @param toolWindow current tool window
   */
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    RegexPluginToolWindow myToolWindow = null;
    try {
      myToolWindow = new RegexPluginToolWindow(toolWindow);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ContentFactory contentFactory = ContentFactory.getInstance();
    Content content = contentFactory.createContent(myToolWindow.getContent(), "", false);
    toolWindow.getContentManager().addContent(content);
  }

}
