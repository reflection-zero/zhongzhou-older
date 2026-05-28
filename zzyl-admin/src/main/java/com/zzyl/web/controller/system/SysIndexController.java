package com.zzyl.web.controller.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 首页
 *
 * @author ruoyi
 */
@Controller
public class SysIndexController
{
    /**
     * 访问首页，转发到前端 SPA
     */
    @RequestMapping("/")
    public String index()
    {
        return "forward:/index.html";
    }
}
