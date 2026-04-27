package io.github.xxyopen.novel.core.ai;

import io.github.xxyopen.novel.dao.entity.AiCharacter;
import io.github.xxyopen.novel.dao.entity.BookInfo;

/**
 * AI Prompt 模板工程
 * 根据场景和角色设定构建 System Prompt
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
public class AiPromptTemplate {

    private AiPromptTemplate() {
    }

    /**
     * 基础系统提示词
     */
    private static final String BASE_SYSTEM_PROMPT = """
        你是一位资深的中国网络小说写作助手，精通玄幻、都市、言情、科幻、悬疑等各类网文题材。
        你的职责是帮助作家提升创作效率和作品质量。
        请严格遵守以下原则：
        1. 保持中文网络小说的语言风格和叙事节奏
        2. 情节推进自然，人物性格一致
        3. 绝不生成色情、暴力、政治敏感、违法违禁内容
        4. 对话生动，场景描写有画面感
        5. 适当埋设伏笔，保持读者阅读兴趣
        """;

    /**
     * 构建通用对话的 System Prompt
     */
    public static String buildChatSystemPrompt(AiCharacter character, BookInfo bookInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append(BASE_SYSTEM_PROMPT);

        if (bookInfo != null) {
            sb.append("\n\n【当前作品信息】\n");
            sb.append("小说名称：").append(bookInfo.getBookName()).append("\n");
            sb.append("作品类型：").append(bookInfo.getCategoryName()).append("\n");
            if (bookInfo.getBookDesc() != null) {
                sb.append("作品简介：").append(bookInfo.getBookDesc()).append("\n");
            }
        }

        if (character != null) {
            sb.append("\n【角色/文风设定】\n");
            if (character.getPersona() != null) {
                sb.append("世界观/人设：").append(character.getPersona()).append("\n");
            }
            if (character.getStyle() != null) {
                sb.append("文风要求：").append(character.getStyle()).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 构建续写场景的 System Prompt
     */
    public static String buildContinueSystemPrompt(AiCharacter character, BookInfo bookInfo,
        String context, Integer targetLength) {
        StringBuilder sb = new StringBuilder();
        sb.append(BASE_SYSTEM_PROMPT);
        sb.append("\n\n当前任务：根据前文进行续写。");

        if (targetLength != null && targetLength > 0) {
            sb.append("续写长度约为 ").append(targetLength).append(" 字。");
        }
        sb.append("请注意保持与原文一致的语言风格和人物性格，情节推进自然，每段结尾可留有悬念。\n");

        if (bookInfo != null) {
            sb.append("\n【作品信息】\n小说：").append(bookInfo.getBookName());
            sb.append(" | 类型：").append(bookInfo.getCategoryName()).append("\n");
        }

        if (character != null) {
            sb.append("\n【文风设定】\n");
            if (character.getPersona() != null) {
                sb.append(character.getPersona()).append("\n");
            }
            if (character.getStyle() != null) {
                sb.append(character.getStyle()).append("\n");
            }
        }

        if (context != null && !context.isBlank()) {
            sb.append("\n【前文】\n").append(context).append("\n\n请继续续写：");
        }

        return sb.toString();
    }

    /**
     * 构建润色场景的 System Prompt
     */
    public static String buildPolishSystemPrompt(AiCharacter character) {
        StringBuilder sb = new StringBuilder();
        sb.append(BASE_SYSTEM_PROMPT);
        sb.append("\n\n当前任务：对提供的文本进行润色优化。\n");
        sb.append("要求：\n");
        sb.append("1. 保持原意不变\n");
        sb.append("2. 修正语病和不通顺的表达\n");
        sb.append("3. 增强画面感和代入感\n");
        sb.append("4. 优化对话，使其更自然生动\n");
        sb.append("5. 直接输出润色后的文本，不要添加解释\n");

        if (character != null && character.getStyle() != null) {
            sb.append("\n【文风要求】\n").append(character.getStyle()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 构建大纲生成场景的 System Prompt
     */
    public static String buildOutlineSystemPrompt() {
        return BASE_SYSTEM_PROMPT + """
            \n\n当前任务：根据提供的关键词或创意，生成一份详细的小说大纲。
            大纲应包含以下内容：
            1. 核心创意与卖点
            2. 世界观设定
            3. 主要角色及人物关系
            4. 主线剧情（分卷/分阶段）
            5. 关键转折点与高潮设计
            6. 结局走向建议
            请结构清晰，便于作家直接参考使用。
            """;
    }
}
