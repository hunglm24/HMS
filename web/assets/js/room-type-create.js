(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var form = document.getElementById('roomTypeCreateForm');

        if (!form) {
            return;
        }

        var nameInput = document.getElementById('roomTypeName');
        var descriptionInput = document.getElementById('roomTypeDescription');
        var capacityInput = document.getElementById('roomTypeCapacity');
        var basePriceInput = document.getElementById('roomTypeBasePrice');
        var sizeInput = document.getElementById('roomTypeSizeM2');
        var coverInput = document.getElementById('roomTypeCoverImage');
        var uploadZone = form.querySelector('[data-room-type-upload-zone]');
        var uploadTrigger = form.querySelector('[data-room-type-upload-trigger]');
        var fileNameEl = form.querySelector('[data-room-type-file-name]');
        var coverPreview = document.getElementById('roomTypeCoverPreview');
        var coverPlaceholder = document.getElementById('roomTypeCoverPlaceholder');
        var existingCoverSrc = coverPreview ? coverPreview.getAttribute('data-original-src') : '';
        var statusRadios = Array.prototype.slice.call(form.querySelectorAll('input[name="status"]'));
        var numberFormatter = new Intl.NumberFormat('vi-VN');
        var previewObjectUrl = null;
        var allowedMimeTypes = ['image/jpeg', 'image/png', 'image/webp'];
        var allowedExtensions = ['jpg', 'jpeg', 'png', 'webp'];
        var maxFileSize = 5 * 1024 * 1024;

        function getFieldEl(input) {
            return input ? input.closest('.room-type-create-field') : null;
        }

        function ensureErrorNode(fieldEl) {
            if (!fieldEl) {
                return null;
            }

            var error = fieldEl.querySelector('.room-type-create-field__error');
            if (!error) {
                error = document.createElement('div');
                error.className = 'room-type-create-field__error';
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
            var errorEl = fieldEl ? fieldEl.querySelector('.room-type-create-field__error') : null;

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

        function resetCoverPreview() {
            if (previewObjectUrl) {
                URL.revokeObjectURL(previewObjectUrl);
                previewObjectUrl = null;
            }

            if (coverPreview) {
                if (existingCoverSrc) {
                    coverPreview.src = existingCoverSrc;
                    coverPreview.hidden = false;
                } else {
                    coverPreview.hidden = true;
                    coverPreview.removeAttribute('src');
                }
            }

            if (coverPlaceholder) {
                coverPlaceholder.hidden = !!existingCoverSrc;
            }

            if (fileNameEl) {
                fileNameEl.textContent = '';
            }
        }

        function setCoverPreview(file) {
            resetCoverPreview();
            if (!file || !coverPreview) {
                return;
            }

            previewObjectUrl = URL.createObjectURL(file);
            coverPreview.src = previewObjectUrl;
            coverPreview.hidden = false;

            if (coverPlaceholder) {
                coverPlaceholder.hidden = true;
            }

            if (fileNameEl) {
                fileNameEl.textContent = file.name;
            }
        }

        function getSelectedStatusRadio() {
            return statusRadios.find(function (input) {
                return input.checked;
            });
        }

        function validateName() {
            var value = normalizeText(nameInput.value);

            if (!value) {
                setFieldError(nameInput, 'Room type name is required.');
                return false;
            }
            if (value.length < 3) {
                setFieldError(nameInput, 'Room type name must be at least 3 characters.');
                return false;
            }
            if (value.length > 100) {
                setFieldError(nameInput, 'Room type name must not exceed 100 characters.');
                return false;
            }

            clearFieldError(nameInput);
            return true;
        }

        function validateDescription() {
            var value = normalizeText(descriptionInput.value);

            if (value.length > 500) {
                setFieldError(descriptionInput, 'Description must not exceed 500 characters.');
                return false;
            }

            clearFieldError(descriptionInput);
            return true;
        }

        function validateCapacity() {
            var raw = normalizeText(capacityInput.value);
            var value = Number(raw);

            if (!raw) {
                setFieldError(capacityInput, 'Capacity is required.');
                return false;
            }
            if (!Number.isInteger(value) || value < 1) {
                setFieldError(capacityInput, 'Capacity must be a positive whole number.');
                return false;
            }

            clearFieldError(capacityInput);
            return true;
        }

        function validateBasePrice() {
            var value = parseDigits(basePriceInput.value);

            if (!Number.isFinite(value) || value <= 0) {
                setFieldError(basePriceInput, 'Base price is required and must be greater than 0.');
                return false;
            }

            clearFieldError(basePriceInput);
            basePriceInput.value = numberFormatter.format(value);
            return true;
        }

        function validateSize() {
            if (!sizeInput) {
                return true;
            }

            var raw = normalizeText(sizeInput.value);

            if (!raw) {
                clearFieldError(sizeInput);
                return true;
            }

            var value = Number(raw);
            if (Number.isNaN(value) || value <= 0) {
                setFieldError(sizeInput, 'Room size must be a positive number.');
                return false;
            }

            clearFieldError(sizeInput);
            return true;
        }

        function validateStatus() {
            var checked = getSelectedStatusRadio();

            if (!checked) {
                setFieldError(statusRadios[0], 'Status is required.');
                return false;
            }

            statusRadios.forEach(function (input) {
                clearFieldError(input);
            });
            return true;
        }

        function validateCoverImage() {
            if (!coverInput) {
                return true;
            }

            var file = coverInput.files && coverInput.files[0];

            if (!file) {
                clearFieldError(coverInput);
                resetCoverPreview();
                return true;
            }

            var extension = (file.name || '').split('.').pop().toLowerCase();

            if (file.size > maxFileSize) {
                setFieldError(coverInput, 'Cover image must be 5 MB or smaller.');
                coverInput.value = '';
                resetCoverPreview();
                return false;
            }

            if (allowedExtensions.indexOf(extension) === -1 || (file.type && allowedMimeTypes.indexOf(file.type) === -1)) {
                setFieldError(coverInput, 'Cover image must be JPG, JPEG, PNG, or WEBP.');
                coverInput.value = '';
                resetCoverPreview();
                return false;
            }

            clearFieldError(coverInput);
            setCoverPreview(file);
            return true;
        }

        function validateForm() {
            var validators = [
                { valid: validateName(), input: nameInput },
                { valid: validateDescription(), input: descriptionInput },
                { valid: validateCapacity(), input: capacityInput },
                { valid: validateBasePrice(), input: basePriceInput },
                { valid: validateSize(), input: sizeInput },
                { valid: validateStatus(), input: statusRadios[0] },
                { valid: validateCoverImage(), input: coverInput }
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

        function wireInputValidation(input, validator) {
            if (!input) {
                return;
            }

            input.addEventListener('input', function () {
                clearFieldError(input);
            });

            input.addEventListener('blur', function () {
                validator();
            });
        }

        wireInputValidation(nameInput, validateName);
        wireInputValidation(descriptionInput, validateDescription);
        wireInputValidation(capacityInput, validateCapacity);
        wireInputValidation(sizeInput, validateSize);

        if (basePriceInput) {
            basePriceInput.addEventListener('input', function () {
                var digits = String(basePriceInput.value || '').replace(/[^\d]/g, '');
                basePriceInput.value = digits ? numberFormatter.format(parseInt(digits, 10)) : '';
                clearFieldError(basePriceInput);
            });

            basePriceInput.addEventListener('blur', validateBasePrice);
        }

        statusRadios.forEach(function (input) {
            input.addEventListener('change', validateStatus);
        });

        if (coverInput) {
            coverInput.addEventListener('change', validateCoverImage);
        }

        if (uploadTrigger && coverInput) {
            uploadTrigger.addEventListener('click', function () {
                coverInput.click();
            });
        }

        if (uploadZone && coverInput) {
            uploadZone.addEventListener('click', function (event) {
                var clickedButton = event.target && event.target.closest && event.target.closest('[data-room-type-upload-trigger]');
                if (!clickedButton) {
                    coverInput.click();
                }
            });

            uploadZone.addEventListener('keydown', function (event) {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    coverInput.click();
                }
            });

            uploadZone.addEventListener('dragover', function (event) {
                event.preventDefault();
                uploadZone.classList.add('is-dragover');
            });

            uploadZone.addEventListener('dragleave', function () {
                uploadZone.classList.remove('is-dragover');
            });

            uploadZone.addEventListener('drop', function (event) {
                event.preventDefault();
                uploadZone.classList.remove('is-dragover');

                if (!event.dataTransfer || !event.dataTransfer.files || !event.dataTransfer.files.length) {
                    return;
                }

                var transfer = new DataTransfer();
                transfer.items.add(event.dataTransfer.files[0]);
                coverInput.files = transfer.files;
                validateCoverImage();
            });
        }

        if (basePriceInput && normalizeText(basePriceInput.value)) {
            var initialDigits = parseDigits(basePriceInput.value);
            if (Number.isFinite(initialDigits) && initialDigits > 0) {
                basePriceInput.value = numberFormatter.format(initialDigits);
            }
        }

        form.addEventListener('submit', function (event) {
            if (!validateForm()) {
                event.preventDefault();
            }
        });
    });
})();
