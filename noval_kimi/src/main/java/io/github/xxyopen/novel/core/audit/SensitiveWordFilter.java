package io.github.xxyopen.novel.core.audit;

import io.github.xxyopen.novel.dao.entity.AuditSensitiveWord;
import io.github.xxyopen.novel.dao.mapper.AuditSensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 基于 AC 自动机的敏感词过滤器
 * 支持中文敏感词、快速多模式匹配
 *
 * @author xiongxiaoyang
 * @date 2026/04/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveWordFilter {

    private final AuditSensitiveWordMapper sensitiveWordMapper;

    private volatile TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        reload();
    }

    @Scheduled(fixedRate = 300000)
    public void reload() {
        List<AuditSensitiveWord> words = sensitiveWordMapper.selectList(null);
        TrieNode newRoot = new TrieNode();
        for (AuditSensitiveWord word : words) {
            if (word.getIsEnabled() == 1) {
                insert(newRoot, word.getWord(), word.getCategory(), word.getLevel());
            }
        }
        buildFailureLinks(newRoot);
        this.root = newRoot;
        log.info("敏感词库加载完成，共 {} 个", words.size());
    }

    public FilterResult match(String text) {
        if (text == null || text.isEmpty()) {
            return new FilterResult(true, Collections.emptyList());
        }
        List<HitResult> hits = new ArrayList<>();
        TrieNode current = root;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            while (current != root && current.children.get(ch) == null) {
                current = current.fail;
            }
            TrieNode next = current.children.get(ch);
            if (next != null) {
                current = next;
            } else {
                current = root;
                continue;
            }
            TrieNode temp = current;
            while (temp != root) {
                if (temp.isEnd) {
                    int start = i - temp.depth + 1;
                    hits.add(new HitResult(
                        text.substring(start, i + 1),
                        temp.category,
                        temp.level,
                        start, i
                    ));
                }
                temp = temp.fail;
            }
        }
        boolean isClean = hits.isEmpty();
        return new FilterResult(isClean, hits);
    }

    public String replace(String text) {
        if (text == null || text.isEmpty()) return text;
        FilterResult result = match(text);
        if (result.isClean()) return text;
        StringBuilder sb = new StringBuilder(text);
        List<HitResult> hits = result.getHits();
        hits.sort((a, b) -> b.start - a.start);
        for (HitResult hit : hits) {
            if (hit.level >= 2) {
                for (int i = hit.start; i <= hit.end; i++) sb.setCharAt(i, '*');
            }
        }
        return sb.toString();
    }

    private void insert(TrieNode root, String word, String category, Integer level) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current = current.children.computeIfAbsent(ch, k -> new TrieNode());
        }
        current.isEnd = true;
        current.category = category;
        current.level = level != null ? level : 1;
        current.depth = word.length();
    }

    private void buildFailureLinks(TrieNode root) {
        Queue<TrieNode> queue = new LinkedList<>();
        root.fail = root;
        for (TrieNode node : root.children.values()) {
            node.fail = root;
            queue.offer(node);
        }
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char ch = entry.getKey();
                TrieNode child = entry.getValue();
                TrieNode fail = current.fail;
                while (fail != root && fail.children.get(ch) == null) {
                    fail = fail.fail;
                }
                if (fail.children.get(ch) != null && fail.children.get(ch) != child) {
                    child.fail = fail.children.get(ch);
                } else {
                    child.fail = root;
                }
                queue.offer(child);
            }
        }
    }

    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        TrieNode fail;
        boolean isEnd = false;
        String category = "other";
        int level = 1;
        int depth = 0;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class FilterResult {
        private boolean clean;
        private List<HitResult> hits;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class HitResult {
        private String word;
        private String category;
        private int level;
        private int start;
        private int end;
    }
}
