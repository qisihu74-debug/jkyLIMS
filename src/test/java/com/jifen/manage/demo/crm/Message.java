package com.jifen.manage.demo.crm;
import com.aliyun.tea.*;
import com.aliyun.dysmsapi20170525.*;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.*;
import com.aliyun.teaopenapi.models.*;
/**
 * @author gjl
 * @version V1.0
 * @Package com.jifen.manage.demo.crm
 * @desc
 * @date 2021/9/16 9:20
 * @Copyright © 河南交科院
 */
public class Message {
    public com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }

    public void main(String[] args_) throws Exception {
        com.aliyun.dysmsapi20170525.Client client = createClient("accessKeyId", "accessKeySecret");
        AddSmsSignRequest addSmsSignRequest = new AddSmsSignRequest()
                .setResourceOwnerAccount("test")
                .setResourceOwnerId(1L)
                .setSignName("test")
                .setRemark("test");
        // 复制代码运行请自行打印 API 的返回值
        client.addSmsSign(addSmsSignRequest);
    }
}
