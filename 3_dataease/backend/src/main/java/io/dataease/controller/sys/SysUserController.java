package io.dataease.controller.sys;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.base.domain.SysRole;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.request.LdapAddRequest;
import io.dataease.controller.sys.request.SysUserCreateRequest;
import io.dataease.controller.sys.request.SysUserPwdRequest;
import io.dataease.controller.sys.request.SysUserStateRequest;
import io.dataease.controller.sys.response.RoleUserItem;
import io.dataease.controller.sys.response.SysUserGridResponse;
import io.dataease.service.sys.SysRoleService;
import io.dataease.service.sys.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@RestController
@Api(tags = "系统：用户管理")
@ApiSupport(order = 220)
@RequestMapping("/api/user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private SysRoleService sysRoleService;

    @ApiOperation("查询用户")
    @PostMapping("/userGrid/{goPage}/{pageSize}")
    public Pager<List<SysUserGridResponse>> userGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, sysUserService.query(request));
    }
    /*public Pager<List<SysUserGridResponse>> userGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody UserGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, sysUserService.query(request));
    }*/

    @ApiOperation("创建用户")
    @PostMapping("/create")
    public void create(@RequestBody SysUserCreateRequest request){
        sysUserService.save(request);
    }

    @ApiOperation("更新用户")
    @PostMapping("/update")
    public void update(@RequestBody SysUserCreateRequest request){
        sysUserService.update(request);
    }

    @ApiOperation("删除用户")
    @PostMapping("/delete/{userId}")
    public void delete(@PathVariable("userId") Long userId){
        sysUserService.delete(userId);
    }


    @ApiOperation("更新用户状态")
    @PostMapping("/updateStatus")
    public void updateStatus(@RequestBody SysUserStateRequest request){
        sysUserService.updateStatus(request);
    }

    @ApiOperation("更新当前用户密码")
    @PostMapping("/updatePwd")
    public void updatePwd(@RequestBody SysUserPwdRequest request){

        sysUserService.updatePwd(request);
    }
    @ApiOperation("更新指定用户密码")
    @PostMapping("/adminUpdatePwd")
    public void adminUpdatePwd(@RequestBody SysUserPwdRequest request){
        sysUserService.adminUpdatePwd(request);
    }


    @ApiOperation("当前用户信息")
    @PostMapping("/personInfo")
    public CurrentUserDto personInfo() {
        CurrentUserDto user = AuthUtils.getUser();
        return user;
    }

    @ApiIgnore
    @ApiOperation("更新个人信息")
    @PostMapping("/updatePersonInfo")
    public void updatePersonInfo(@RequestBody SysUserCreateRequest request){
        sysUserService.updatePersonInfo(request);
    }

    @ApiOperation("设置语言")
    @PostMapping("/setLanguage/{language}")
    public void setLanguage(@PathVariable String language) {
        CurrentUserDto user = AuthUtils.getUser();
        Optional.ofNullable(language).ifPresent(currentLanguage -> {
            if (!currentLanguage.equals(user.getLanguage())) {
                sysUserService.setLanguage(user.getUserId(), currentLanguage);
            }
        });
    }

    @ApiOperation("查询所有角色")
    @PostMapping("/all")
    public List<RoleUserItem> all(){
        return sysRoleService.allRoles();
    }



    @ApiOperation("查询角色")
    @PostMapping("/roleGrid/{goPage}/{pageSize}")
    public Pager<List<SysRole>> roleGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        Pager<List<SysRole>> listPager = PageUtils.setPageInfo(page, sysRoleService.query(request));
        return listPager;
    }


    @ApiOperation("同步用户")
    @PostMapping("/sync")
    public void importLdap(@RequestBody LdapAddRequest request) {
        sysUserService.saveLdapUsers(request);
    }

    @ApiOperation("已同步用户")
    @PostMapping("/existLdapUsers")
    public List<String> getExistLdapUsers() {
        return sysUserService.ldapUserNames();
    }

}
