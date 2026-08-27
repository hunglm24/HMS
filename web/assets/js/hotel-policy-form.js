(function () {
  const form = document.querySelector(".policy-form");
  if (!form) {
    return;
  }

  const titleInput = form.querySelector('input[name="title"]');
  const contentInput = form.querySelector('textarea[name="content"]');
  const toggleButton = form.querySelector(
    '[data-policy-action="toggle-numbering"]',
  );
  const toggleIcon = form.querySelector("[data-policy-icon]");

  function buildPattern(source) {
    try {
      return new RegExp(source, "u");
    } catch (error) {
      return null;
    }
  }

  const titlePattern = buildPattern(
    "^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N}\\s.,;:!?()\\-_/&'\"+@#%]{2,150}$",
  );
  const contentPattern = buildPattern(
    "^(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{N}\\s.,;:!?()\\-_/&'\"+@#%]{10,5000}$",
  );

  const titleLengthMessage = "Tiêu đề phải có từ 2 đến 150 ký tự.";
  const contentLengthMessage = "Nội dung phải có từ 10 đến 5000 ký tự.";
  const titleMessage =
    "Tiêu đề chỉ được chứa chữ, số, khoảng trắng và một số ký tự đặc biệt hợp lệ.";
  const contentMessage =
    "Nội dung chỉ được chứa chữ, số, khoảng trắng, xuống dòng và một số ký tự đặc biệt hợp lệ.";

  function splitLines(value) {
    return String(value || "")
      .replace(/\r\n/g, "\n")
      .split("\n");
  }

  function stripNumbering(line) {
    return line.replace(/^\s*\d+\.\s*/, "").trim();
  }

  function numberLines(value) {
    const lines = splitLines(value)
      .map(stripNumbering)
      .filter((line) => line.length > 0);

    return lines.map((line, index) => `${index + 1}. ${line}`).join("\n");
  }

  function clearNumbering(value) {
    return splitLines(value).map(stripNumbering).join("\n").trim();
  }

  function updateContent(nextValue) {
    if (!contentInput) {
      return;
    }

    contentInput.value = nextValue;
    contentInput.focus();
    contentInput.dispatchEvent(new Event("input", { bubbles: true }));
    contentInput.dispatchEvent(new Event("change", { bubbles: true }));
  }

  function validateLengthField(input, minLength, maxLength, message) {
    if (!input) {
      return true;
    }

    const value = input.value.trim();
    if (!value) {
      input.setCustomValidity("");
      return true;
    }

    if (value.length < minLength || value.length > maxLength) {
      input.setCustomValidity(message);
      return false;
    }

    input.setCustomValidity("");
    return true;
  }

  function validateRegexField(input, pattern, message) {
    if (!input || !pattern) {
      return true;
    }

    const value = input.value.trim();
    if (!value) {
      input.setCustomValidity("");
      return true;
    }

    if (!pattern.test(value)) {
      input.setCustomValidity(message);
      return false;
    }

    input.setCustomValidity("");
    return true;
  }

  function isNumberedContent(value) {
    const lines = splitLines(value)
      .map((line) => line.trim())
      .filter(Boolean);
    if (!lines.length) {
      return false;
    }

    return lines.every((line) => /^\d+\.\s+/.test(line));
  }

  function syncToggleButton() {
    if (!toggleButton || !toggleIcon || !contentInput) {
      return;
    }

    const numbered = isNumberedContent(contentInput.value);
    toggleIcon.className = "bi bi-list-ol";
    toggleButton.setAttribute(
      "aria-label",
      numbered ? "Bỏ đánh số" : "Đánh số dòng",
    );
    toggleButton.setAttribute(
      "title",
      numbered ? "Bỏ đánh số" : "Đánh số dòng",
    );
    toggleButton.dataset.state = numbered ? "numbered" : "plain";
  }

  function bindQuickActions() {
    if (!toggleButton) {
      return;
    }

    toggleButton.addEventListener("click", () => {
      if (!contentInput) {
        return;
      }

      if (isNumberedContent(contentInput.value)) {
        updateContent(clearNumbering(contentInput.value));
      } else {
        updateContent(numberLines(contentInput.value));
      }

      syncToggleButton();
    });
  }

  function bindContentTracking() {
    if (!contentInput) {
      return;
    }

    contentInput.addEventListener("input", syncToggleButton);
    contentInput.addEventListener("change", syncToggleButton);
  }

  function bindValidation() {
    function validateTitle() {
      const lengthValid = validateLengthField(
        titleInput,
        2,
        150,
        titleLengthMessage,
      );
      if (!lengthValid) {
        return false;
      }

      return validateRegexField(titleInput, titlePattern, titleMessage);
    }

    function validateContent() {
      const lengthValid = validateLengthField(
        contentInput,
        10,
        5000,
        contentLengthMessage,
      );
      if (!lengthValid) {
        return false;
      }

      return validateRegexField(contentInput, contentPattern, contentMessage);
    }

    if (titleInput) {
      titleInput.addEventListener("input", validateTitle);
      titleInput.addEventListener("blur", validateTitle);
    }

    if (contentInput) {
      contentInput.addEventListener("input", validateContent);
      contentInput.addEventListener("blur", validateContent);
    }

    form.addEventListener("submit", (event) => {
      const titleValid = titleInput ? validateTitle() : true;
      const contentValid = contentInput ? validateContent() : true;

      if (!titleValid || !contentValid || !form.reportValidity()) {
        event.preventDefault();
      }
    });
  }

  syncToggleButton();
  bindQuickActions();
  bindContentTracking();
  bindValidation();
})();
