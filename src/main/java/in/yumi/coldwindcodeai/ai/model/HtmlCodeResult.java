package in.yumi.coldwindcodeai.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成一个完整独立的单页面 HTML 文件，要求响应式、美观、无外部依赖")
@Data
public class HtmlCodeResult {

    @Description("完整的 HTML 文档，包含 DOCTYPE、head 中的 style/meta 和 body，是一个可直接运行的独立页面")
    private String htmlCode;

    @Description("对生成页面的设计风格、布局和功能的简要说明描述")
    private String description;
}
