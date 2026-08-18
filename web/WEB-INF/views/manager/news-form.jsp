<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty news.id ? 'Edit News' : 'Add New News'} - Admin Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- TinyMCE -->
    <script src="https://cdn.tiny.cloud/1/43srccoaywvqahbipb7nz0kb1g1e3xfng5vhmx77cctfcqi3/tinymce/6/tinymce.min.js" referrerpolicy="origin"></script>
    <script>
        tinymce.init({
            selector: '#content',
            plugins: 'advlist autolink lists link image charmap preview anchor pagebreak',
            toolbar_mode: 'floating',
            height: 500
        });
    </script>
    <!-- Cloudinary Upload Widget -->
    <script src="https://upload-widget.cloudinary.com/global/all.js" type="text/javascript"></script>
</head>
<body>
    <div class="container mt-5 mb-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>${not empty news.id ? 'Edit News' : 'Add New News'}</h2>
            <a href="${pageContext.request.contextPath}/manager/news" class="btn btn-outline-secondary">
                <i class="fas fa-arrow-left"></i> Back to List
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/manager/news/create" method="POST">
            <c:if test="${not empty news.id}">
                <!-- Change action path dynamically if editing -->
                <script>document.forms[0].action = "${pageContext.request.contextPath}/manager/news/edit";</script>
            </c:if>

            <input type="hidden" name="id" value="${news.id}">
            <input type="hidden" name="thumbnailUrl" id="thumbnailUrl" value="${news.thumbnailUrl}">

            <div class="row">
                <div class="col-md-8">
                    <div class="mb-3">
                        <label for="title" class="form-label fw-bold">Title</label>
                        <input type="text" class="form-control" id="title" name="title" value="${news.title}" required>
                    </div>

                    <div class="mb-3">
                        <label for="content" class="form-label fw-bold">Content</label>
                        <textarea id="content" name="content">${news.content}</textarea>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="card shadow-sm mb-3">
                        <div class="card-header bg-light fw-bold">Publish Options</div>
                        <div class="card-body">
                            <div class="mb-3">
                                <label for="status" class="form-label">Status</label>
                                <select class="form-select" id="status" name="status">
                                    <option value="DRAFT" ${news.status == 'DRAFT' ? 'selected' : ''}>Draft</option>
                                    <option value="PUBLISHED" ${news.status == 'PUBLISHED' ? 'selected' : ''}>Published</option>
                                    <option value="HIDDEN" ${news.status == 'HIDDEN' ? 'selected' : ''}>Hidden</option>
                                </select>
                            </div>
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="fas fa-save"></i> Save News
                            </button>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-header bg-light fw-bold">Thumbnail Image</div>
                        <div class="card-body text-center">
                            <img id="thumbnailPreview" src="${not empty news.thumbnailUrl ? news.thumbnailUrl : 'https://via.placeholder.com/300x200?text=No+Image'}" alt="Thumbnail" class="img-fluid mb-3 rounded" style="max-height: 200px; object-fit: cover;">
                            
                            <button type="button" id="upload_widget" class="btn btn-outline-secondary w-100">
                                <i class="fas fa-cloud-upload-alt"></i> Upload Thumbnail
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <script>
        // Set up Cloudinary Widget. 
        // Note: Please replace 'your_cloud_name' and 'your_upload_preset' with actual Cloudinary credentials
        var myWidget = cloudinary.createUploadWidget({
            cloudName: 'hms_cloud', 
            apiKey: '119113455128334',
            uploadPreset: 'ml_default',
            cropping: true,
            multiple: false
        }, (error, result) => { 
            if (!error && result && result.event === "success") { 
                console.log('Done! Here is the image info: ', result.info); 
                document.getElementById("thumbnailUrl").value = result.info.secure_url;
                document.getElementById("thumbnailPreview").src = result.info.secure_url;
            }
        });

        document.getElementById("upload_widget").addEventListener("click", function(){
            myWidget.open();
        }, false);
    </script>
</body>
</html>
