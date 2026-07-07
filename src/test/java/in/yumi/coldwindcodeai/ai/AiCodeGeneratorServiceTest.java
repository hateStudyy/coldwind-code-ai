package in.yumi.coldwindcodeai.ai;

import in.yumi.coldwindcodeai.ai.model.HtmlCodeResult;
import in.yumi.coldwindcodeai.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个 coldwind（作者） 的 hello world 网站");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个程序员鱼皮的留言板");
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getHtmlCode());
        Assertions.assertNotNull(result.getCssCode());
        Assertions.assertNotNull(result.getJsCode());
    }

    @Test
    void testChatMemory() {
        var result = aiCodeGeneratorService.generateHtmlCode(1, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode(1, "不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode(2, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateHtmlCode(2, "不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
    }

}
