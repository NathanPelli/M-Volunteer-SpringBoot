package com.github.oneone1995.mvolunteer.web;

import com.github.oneone1995.mvolunteer.config.result.ResultStatus;
import com.github.oneone1995.mvolunteer.domain.Activity;
import com.github.oneone1995.mvolunteer.domain.ActivityDetails;
import com.github.oneone1995.mvolunteer.domain.HomeActivity;
import com.github.oneone1995.mvolunteer.model.ResultModel;
import com.github.oneone1995.mvolunteer.service.ActivityService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Created by wangl on 2017/2/18.
 * 活动相关API接口
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * APP首页的活动列表分页获取接口
     * @param page 页码
     * @param rows 单页数量
     * @param coordLong 经度
     * @param coordLat 维度
     */
    @GetMapping
    public ResponseEntity<?> getActivities(
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "coordLong") double coordLong,
            @RequestParam(value = "coordLat") double coordLat) {

        PageInfo<HomeActivity> homeActivityPageInfo = activityService.getHomeActivityPageInfo(
                page, rows, coordLong, coordLat);

        if (homeActivityPageInfo == null) {
            return new ResponseEntity<>(ResultModel.error(ResultStatus.ACTIVITY_NOT_FOUNT), HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(ResultModel.ok(homeActivityPageInfo), HttpStatus.OK);
    }


    /**
     * 根据活动所属类别分页获取符合规则的活动列表API接口
     * @param page 页码
     * @param rows 但也数量
     * @param coordLong 当前经度
     * @param coordLat 当前维度
     * @param category 活动类别
     * @param collation 排序规则 0为按时间排序、1为按距离排序
     * @param district 按地区筛选
     */
    @GetMapping("/category")
    public ResponseEntity<?> getActivitiesByCategory(
            @RequestParam(value = "page") Integer page,
            @RequestParam(value = "rows", defaultValue = "10") Integer rows,
            @RequestParam(value = "coordLong") double coordLong,
            @RequestParam(value = "coordLat") double coordLat,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "collation", defaultValue = "0") Integer collation,
            @RequestParam(value = "district", required = false) String district
    ) {
        PageInfo<HomeActivity> categoryActivityPageInfo = activityService.getHomeActivityPageInfo(
                page, rows, coordLong, coordLat, category, collation, district);

        if (categoryActivityPageInfo == null) {
            return new ResponseEntity<>(ResultModel.error(ResultStatus.ACTIVITY_NOT_FOUNT), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(ResultModel.ok(categoryActivityPageInfo), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_VOL') or hasRole('ROLE_ORG')")
    public ResponseEntity<?> getActivityById(
            @PathVariable Integer id,
            @RequestParam(value = "coordLong") double coordLong,
            @RequestParam(value = "coordLat") double coordLat
    ) {
        ActivityDetails activityDetails = activityService.getActivityById(
                id, coordLong, coordLat);

        if (activityDetails == null) {
            return new ResponseEntity<>(ResultModel.error(ResultStatus.ACTIVITY_NOT_FOUNT), HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(ResultModel.ok(activityDetails), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORG')")
    public ResponseEntity<?> postActivity(@RequestBody Activity activity) {
        boolean result = activityService.createActivity(activity);

        if (!result) {
            return new ResponseEntity<>(ResultModel.error(ResultStatus.ACTIVITY_CREATE_FAIL), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(ResultModel.ok("创建活动成功"), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORG')")
    public ResponseEntity<?> putActivity(
        @PathVariable Integer id,
        @RequestParam(value = "activityStatusId") Integer activityStatusId
    ) {
        String result = activityService.updateActivityStatusById(id, activityStatusId);

        if (result.equals("ACTIVITY_NOT_FOUNT")) {
            return new ResponseEntity<Object>(ResultModel.error(ResultStatus.ACTIVITY_NOT_FOUNT), HttpStatus.NOT_FOUND);
        }
        if (result.equals("IMMUTABLE")) {
            return new ResponseEntity<Object>(ResultModel.error(ResultStatus.ACTIVITY_STATUS_IMMUTABLE), HttpStatus.BAD_REQUEST);
        }
        if (result.equals("FAIL")) {
            return new ResponseEntity<Object>(ResultModel.error(ResultStatus.ACTIVITY_STATUS_UPDATE_FAIL), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(ResultModel.ok(result), HttpStatus.OK);
    }
}
