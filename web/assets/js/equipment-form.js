(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var form = document.querySelector('.equipment-form');

        if (!form) {
            return;
        }

        var nameInput = form.querySelector('input[name="name"]');
        var descriptionInput = form.querySelector('textarea[name="description"]');
        var priceInput = form.querySelector('input[name="defaultCompensationPrice"]');
        var statusRadios = Array.prototype.slice.call(form.querySelectorAll('input[name="status"]'));
        var imageInput = form.querySelector('input[name="imageFile"]');
        var previewImage = document.getElementById('equipmentImagePreview');
        var previewPlaceholder = document.getElementById('equipmentImagePlaceholder');
        var originalSrc = previewImage ? previewImage.getAttribute('data-original-src') : '';
        var objectUrl = null;
        var numberFormatter = new Intl.NumberFormat('vi-VN');
        var maxImageSize = 5 * 1024 * 1024;
        var allowedMimeTypes = ['image/jpeg', 'image/png', 'image/webp'];
        var allowedExtensions = ['jpg', 'jpeg', 'png', 'webp'];

        function getFieldEl(input) {
            return input ? input.closest('.equipment-form-field') : null;
        }

        function ensureErrorNode(fieldEl) {
            if (!fieldEl) {
                return null;
            }

            var error = fieldEl.querySelector('.equipment-form-field__error');
            if (!error) {
                error = document.createElement('div');
                error.className = 'equipment-form-field__error';
                error.setAttribute('aria-live', 'polite');
                fieldEl.appendChild(error);
            }
            return error;
        }

        function setFieldError(input, message) {
            var fieldEl = getFieldEl(input);
            var errorEl = ensureErrorNode(fieldEl);

            if (fieldEl) {
                fieldEl.classList.add('is-error');
            }
            if (input) {
                input.setAttribute('aria-invalid', 'true');
                input.setCustomValidity(message || 'Invalid value');
            }
            if (errorEl) {
                errorEl.textContent = message || '';
            }
        }

        function clearFieldError(input) {
            var fieldEl = getFieldEl(input);
            var errorEl = fieldEl ? fieldEl.querySelector('.equipment-form-field__error') : null;

            if (fieldEl) {
                fieldEl.classList.remove('is-error');
            }
            if (input) {
                input.removeAttribute('aria-invalid');
                input.setCustomValidity('');
            }
            if (errorEl) {
                errorEl.textContent = '';
            }
        }

        function normalizeText(value) {
            return (value || '').replace(/\s+/g, ' ').trim();
        }

        function parseDigits(value) {
            var digits = String(value || '').replace(/[^\d]/g, '');
            return digits ? parseInt(digits, 10) : NaN;
        }

        function clearObjectUrl() {
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
                objectUrl = null;
            }
        }

        function showPlaceholder() {
            if (previewImage) {
                previewImage.hidden = true;
                previewImage.removeAttribute('src');
            }
            if (previewPlaceholder) {
                previewPlaceholder.hidden = false;
            }
        }

        function showOriginalImage() {
            if (previewImage && originalSrc) {
                previewImage.src = originalSrc;
                previewImage.hidden = false;
            }
            if (previewPlaceholder) {
                previewPlaceholder.hidden = !!originalSrc;
            }
        }

        function updatePreview(file) {
            clearObjectUrl();

            if (!file) {
                showOriginalImage();
                if (!originalSrc) {
                    showPlaceholder();
                }
                return;
            }

            if (!previewImage) {
                return;
            }

            objectUrl = URL.createObjectURL(file);
            previewImage.src = objectUrl;
            previewImage.hidden = false;

            if (previewPlaceholder) {
                previewPlaceholder.hidden = true;
            }
        }

        function validateName() {
            if (!nameInput) {
                return true;
            }

            var value = normalizeText(nameInput.value);
            if (!value) {
                setFieldError(nameInput, 'Equipment name is required.');
                return false;
            }
            if (value.length < 2) {
                setFieldError(nameInput, 'Equipment name must be at least 2 characters.');
                return false;
            }
            if (value.length > 100) {
                setFieldError(nameInput, 'Equipment name must not exceed 100 characters.');
                return false;
            }

            clearFieldError(nameInput);
            return true;
        }

        function validateDescription() {
            if (!descriptionInput) {
                return true;
            }

            var value = normalizeText(descriptionInput.value);
            if (value.length > 500) {
                setFieldError(descriptionInput, 'Description must not exceed 500 characters.');
                return false;
            }

            clearFieldError(descriptionInput);
            return true;
        }

        function validatePrice() {
            if (!priceInput) {
                return true;
            }

            var value = parseDigits(priceInput.value);
            if (!Number.isFinite(value) || value <= 0) {
                setFieldError(priceInput, 'Compensation price is required and must be greater than 0.');
                return false;
            }

            clearFieldError(priceInput);
            priceInput.value = numberFormatter.format(value);
            return true;
        }

        function validateStatus() {
            if (!statusRadios.length) {
                return true;
            }

            var checked = statusRadios.find(function (input) {
                return input.checked;
            });

            if (!checked) {
                setFieldError(statusRadios[0], 'Status is required.');
                return false;
            }

            statusRadios.forEach(function (input) {
                clearFieldError(input);
            });
            return true;
        }

        function validateImage() {
            if (!imageInput) {
                return true;
            }

            var file = imageInput.files && imageInput.files[0];
            if (!file) {
                clearFieldError(imageInput);
                updatePreview(null);
                return true;
            }

            var extension = (file.name || '').split('.').pop().toLowerCase();
            if (file.size > maxImageSize) {
                setFieldError(imageInput, 'Equipment image must be 5 MB or smaller.');
                imageInput.value = '';
                updatePreview(null);
                return false;
            }

            if (allowedExtensions.indexOf(extension) === -1 || (file.type && allowedMimeTypes.indexOf(file.type) === -1)) {
                setFieldError(imageInput, 'Equipment image must be JPG, PNG, or WEBP.');
                imageInput.value = '';
                updatePreview(null);
                return false;
            }

            clearFieldError(imageInput);
            updatePreview(file);
            return true;
        }

        function validateForm() {
            var validators = [
                { valid: validateName(), input: nameInput },
                { valid: validateDescription(), input: descriptionInput },
                { valid: validatePrice(), input: priceInput },
                { valid: validateStatus(), input: statusRadios[0] },
                { valid: validateImage(), input: imageInput }
            ];

            var firstInvalid = validators.find(function (item) {
                return !item.valid;
            });

            if (firstInvalid && firstInvalid.input) {
                firstInvalid.input.focus();
                if (typeof firstInvalid.input.scrollIntoView === 'function') {
                    firstInvalid.input.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }

            return !firstInvalid;
        }

        if (imageInput) {
            imageInput.addEventListener('change', function () {
                validateImage();
            });
        }

        if (nameInput) {
            nameInput.addEventListener('input', function () {
                clearFieldError(nameInput);
            });
            nameInput.addEventListener('blur', validateName);
        }

        if (descriptionInput) {
            descriptionInput.addEventListener('input', function () {
                clearFieldError(descriptionInput);
            });
            descriptionInput.addEventListener('blur', validateDescription);
        }

        if (priceInput) {
            priceInput.addEventListener('input', function () {
                var digits = String(priceInput.value || '').replace(/[^\d]/g, '');
                priceInput.value = digits ? numberFormatter.format(parseInt(digits, 10)) : '';
                clearFieldError(priceInput);
            });
            priceInput.addEventListener('blur', validatePrice);
        }

        statusRadios.forEach(function (input) {
            input.addEventListener('change', validateStatus);
        });

        form.addEventListener('reset', function () {
            window.setTimeout(function () {
                updatePreview(null);
            }, 0);
        });

        form.addEventListener('submit', function (event) {
            if (!validateForm()) {
                event.preventDefault();
            }
        });
    });
})();
