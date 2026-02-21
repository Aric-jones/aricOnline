import VMdEditor from "@kangc/v-md-editor";
import "@kangc/v-md-editor/lib/style/base-editor.css";
import "@kangc/v-md-editor/lib/theme/style/vuepress.css";
import vuepressTheme from "@kangc/v-md-editor/lib/theme/vuepress.js";
import createTodoListPlugin from "@kangc/v-md-editor/lib/plugins/todo-list/index";
import "@kangc/v-md-editor/lib/plugins/todo-list/todo-list.css";
import Prism from "prismjs";
import { App } from "vue";

export default function setupMdEditor(app: App) {
	VMdEditor.use(vuepressTheme, { Prism })
		.use(createTodoListPlugin());
	app.use(VMdEditor);
}
