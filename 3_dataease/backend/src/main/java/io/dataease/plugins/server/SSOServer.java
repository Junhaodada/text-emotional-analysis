package io.dataease.plugins.server;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.oidc.dto.SSOToken;
import io.dataease.plugins.xpack.oidc.dto.SSOUserInfo;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;
import io.dataease.service.sys.SysUserService;

@RequestMapping("/sso")
@Controller
public class SSOServer {

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/callBack")
    public ModelAndView callBack(@RequestParam("code") String code, @RequestParam("state") String state) {
        Map<String, OidcXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((OidcXpackService.class));
        if(beansOfType.keySet().size() == 0) {
            DEException.throwException("缺少oidc插件");
        }
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        Boolean suuportOIDC = oidcXpackService.isSuuportOIDC();
        if (!suuportOIDC) {
            DEException.throwException("未开启oidc");
        }
        Map<String, String> config = config(oidcXpackService);
        SSOToken ssoToken = oidcXpackService.requestSsoToken(config, code, state);
        
        SSOUserInfo ssoUserInfo = oidcXpackService.requestUserInfo(config, ssoToken.getAccessToken());
        SysUserEntity sysUserEntity = authUserService.getUserBySub(ssoUserInfo.getSub());
        if(null == sysUserEntity){
            sysUserService.saveOIDCUser(ssoUserInfo);
            sysUserEntity = authUserService.getUserBySub(ssoUserInfo.getSub());
        }
        TokenInfo tokenInfo = TokenInfo.builder().userId(sysUserEntity.getUserId()).username(sysUserEntity.getUsername()).build();
        String realPwd = CodingUtil.md5(sysUserService.defaultPWD());
        String token = JWTUtils.sign(tokenInfo, realPwd);
        ServletUtils.setToken(token);
        HttpServletResponse response = ServletUtils.response();
        
        Cookie cookie_token = new Cookie("Authorization", token);cookie_token.setPath("/");
        Cookie cookie_id_token = new Cookie("IdToken", ssoToken.getIdToken());cookie_id_token.setPath("/");
        Cookie cookie_ac_token = new Cookie("AccessToken", ssoToken.getAccessToken());cookie_ac_token.setPath("/");

        response.addCookie(cookie_token);
        response.addCookie(cookie_id_token);
        response.addCookie(cookie_ac_token);
        ModelAndView modelAndView = new ModelAndView("redirect:/");        
        return modelAndView;
    }
    private Map<String, String> config(OidcXpackService oidcXpackService) {
        List<SysSettingDto> sysSettingDtos = oidcXpackService.oidcSettings();
        Map<String, String> config = sysSettingDtos.stream().collect(Collectors.toMap(SysSettingDto::getParamKey, SysSettingDto::getParamValue));
        return config;
    }
    

    
    
}
