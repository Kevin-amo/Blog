const PLACEHOLDER_REG = /\u0000(\d+)\u0000/g;

function escapeHtml(input = "") {
  return String(input)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function restorePlaceholders(content, stash) {
  return content.replace(PLACEHOLDER_REG, (_, idx) => stash[Number(idx)] || "");
}

function applyInlineMarkdown(raw = "") {
  const stash = [];
  const hold = (html) => {
    stash.push(html);
    return `\u0000${stash.length - 1}\u0000`;
  };

  let text = escapeHtml(raw);

  text = text.replace(/`([^`]+?)`/g, (_, code) => hold(`<code>${code}</code>`));
  text = text.replace(/!\[([^\]]*)\]\((https?:\/\/[^\s)]+)\)/g, (_, alt, url) =>
    hold(`<img src="${url}" alt="${alt}" />`)
  );
  text = text.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g, (_, label, url) =>
    hold(`<a href="${url}" target="_blank" rel="noopener noreferrer">${label}</a>`)
  );

  text = text.replace(/\*\*\*([^*]+?)\*\*\*/g, "<strong><em>$1</em></strong>");
  text = text.replace(/\*\*([^*]+?)\*\*/g, "<strong>$1</strong>");
  text = text.replace(/\*([^*\n]+?)\*/g, "<em>$1</em>");
  text = text.replace(/~~([^~]+?)~~/g, "<del>$1</del>");

  return restorePlaceholders(text, stash);
}

export function parseMarkdownToHtml(markdown = "") {
  const lines = String(markdown).replace(/\r\n/g, "\n").split("\n");
  const output = [];

  let paragraphLines = [];
  let listType = "";
  let listItems = [];
  let inCodeBlock = false;
  let codeLines = [];

  const flushParagraph = () => {
    if (!paragraphLines.length) {
      return;
    }
    const html = paragraphLines.map((line) => applyInlineMarkdown(line)).join("<br />");
    output.push(`<p>${html}</p>`);
    paragraphLines = [];
  };

  const flushList = () => {
    if (!listType || !listItems.length) {
      return;
    }
    const itemsHtml = listItems.map((item) => `<li>${applyInlineMarkdown(item)}</li>`).join("");
    output.push(`<${listType}>${itemsHtml}</${listType}>`);
    listType = "";
    listItems = [];
  };

  const closeAllTextBlocks = () => {
    flushParagraph();
    flushList();
  };

  for (const line of lines) {
    const codeFence = line.match(/^```\s*([\w-]+)?\s*$/);

    if (codeFence) {
      if (inCodeBlock) {
        const codeHtml = escapeHtml(codeLines.join("\n"));
        output.push(`<pre><code>${codeHtml}</code></pre>`);
        codeLines = [];
        inCodeBlock = false;
      } else {
        closeAllTextBlocks();
        inCodeBlock = true;
      }
      continue;
    }

    if (inCodeBlock) {
      codeLines.push(line);
      continue;
    }

    if (!line.trim()) {
      closeAllTextBlocks();
      continue;
    }

    if (/^(-{3,}|\*{3,}|_{3,})\s*$/.test(line)) {
      closeAllTextBlocks();
      output.push("<hr />");
      continue;
    }

    const heading = line.match(/^(#{1,6})\s+(.+)$/);
    if (heading) {
      closeAllTextBlocks();
      const level = heading[1].length;
      output.push(`<h${level}>${applyInlineMarkdown(heading[2])}</h${level}>`);
      continue;
    }

    const quote = line.match(/^>\s?(.*)$/);
    if (quote) {
      closeAllTextBlocks();
      output.push(`<blockquote><p>${applyInlineMarkdown(quote[1])}</p></blockquote>`);
      continue;
    }

    const orderedItem = line.match(/^\d+\.\s+(.+)$/);
    if (orderedItem) {
      flushParagraph();
      if (listType && listType !== "ol") {
        flushList();
      }
      listType = "ol";
      listItems.push(orderedItem[1]);
      continue;
    }

    const unorderedItem = line.match(/^[-*+]\s+(.+)$/);
    if (unorderedItem) {
      flushParagraph();
      if (listType && listType !== "ul") {
        flushList();
      }
      listType = "ul";
      listItems.push(unorderedItem[1]);
      continue;
    }

    flushList();
    paragraphLines.push(line);
  }

  if (inCodeBlock) {
    const codeHtml = escapeHtml(codeLines.join("\n"));
    output.push(`<pre><code>${codeHtml}</code></pre>`);
  }

  closeAllTextBlocks();

  return output.join("\n");
}

export function extractPlainText(markdown = "") {
  return String(markdown)
    .replace(/```[\s\S]*?```/g, " ")
    .replace(/`([^`]+)`/g, "$1")
    .replace(/!\[([^\]]*)\]\([^)]*\)/g, "$1")
    .replace(/\[([^\]]+)\]\([^)]*\)/g, "$1")
    .replace(/[#>*_~\-]/g, " ")
    .replace(/\d+\.\s/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}
