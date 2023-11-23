// package com.sky.controller.admin;
//
// import com.sky.constant.MessageConstant;
// import com.sky.utils.AliOssUtil;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.multipart.MultipartFile;
//
// import java.io.IOException;
// import java.util.UUID;
//
// @RestController
// @Slf4j
// @Api(tags = "公共接口")
// @RequestMapping("/admin/common")
// public class CommonController {
//
//     @Autowired
//     private AliOssUtil aliOssUtil;
//
//     /**
//      * 文件上传
//      *
//      * @return
//      */
//     @PostMapping("/upload")
//     @ApiOperation("文件上传")
//     public <String> upload(MultipartFile file) {
//         log.info("接收图片：{}", file);
//
//         //获取原始文件名
//         String originalFilename = file.getOriginalFilename();
//         //截取出扩展名
//         String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
//         //目标：新建一个唯一的文件名
//         String newFileName = UUID.randomUUID().toString() + extName;
//
//         String url = null;
//         try {
//             url = aliOssUtil.upload(file.getBytes(), newFileName);
//             return .success(url);
//         } catch (IOException e) {
//             e.printStackTrace();
//             log.info("上传文件失败");
//         }
//
//         return .error(MessageConstant.UPLOAD_FAILED);
//     }
// }
