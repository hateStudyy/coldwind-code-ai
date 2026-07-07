package in.yumi.coldwindcodeai.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成一个完整单页网站的三个核心文件：HTML、CSS、JavaScript，要求文件分离、响应式、无外部依赖")
@Data
public class MultiFileCodeResult {

    @Description("index.html 文件内容：只包含网页结构和内容，通过 <link> 引用 style.css，通过 <script> 引用 script.js")
    private String htmlCode;

    @Description("style.css 文件内容：网站所有的样式规则，包含响应式布局，使用 Flexbox 或 Grid")
    private String cssCode;

    @Description("script.js 文件内容：网站所有的交互逻辑，使用原生 JavaScript 实现")
    private String jsCode;

    @Description("对生成页面的设计风格和功能的简要说明描述")
    private String description;
}
