package io.dataease.plugins.server;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.ukey.dto.request.XpackUkeyDto;
import io.dataease.plugins.xpack.ukey.service.UkeyXpackService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import java.util.List;

@RequestMapping("/plugin/ukey")
@RestController
public class XUserKeysServer {


    @PostMapping("info")
    public List<XpackUkeyDto> getUserKeysInfo() {

        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        Long userId = AuthUtils.getUser().getUserId();
        return ukeyXpackService.getUserKeysInfo(userId);
    }

    @PostMapping("validate")
    public String validate(ServletRequest request) {
        // return ApiKeyHandler.getUser(WebUtils.toHttp(request));
        return null;
    }

    @PostMapping("generate")
    public void generateUserKey() {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        Long userId = AuthUtils.getUser().getUserId();
        ukeyXpackService.generateUserKey(userId);
    }

    @PostMapping("delete/{id}")
    public void deleteUserKey(@PathVariable Long id) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        ukeyXpackService.deleteUserKey(id);
    }

    @PostMapping("changeStatus/{id}")
    public void changeStatus(@PathVariable Long id) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        ukeyXpackService.switchStatus(id);
    }

    /*@GetMapping("active/{id}")
    public void activeUserKey(@PathVariable Long id) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        ukeyXpackService.activeUserKey(id);
    }

    @GetMapping("disable/{id}")
    public void disabledUserKey(@PathVariable Long id) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        ukeyXpackService.disableUserKey(id);
    }*/
}
