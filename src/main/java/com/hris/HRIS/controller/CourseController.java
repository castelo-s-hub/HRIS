package com.hris.HRIS.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hris.HRIS.dto.ApiResponse;
import com.hris.HRIS.model.CourseModel;
import com.hris.HRIS.repository.CourseRepository;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/lms/course")
public class CourseController {
    @Autowired
    CourseRepository courseRepository;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> createCourse(@RequestBody CourseModel courseModel) {
        courseModel.setCourseCreatedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        courseRepository.save(courseModel);

        ApiResponse apiResponse = new ApiResponse("Course created successfully.");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/get/all")
    public List<CourseModel> getAllCourses(){
        return courseRepository.findAll();
    }

    @GetMapping("/get/id/{id}")
    public ResponseEntity<CourseModel> getCourseById(@PathVariable String id){
        Optional<CourseModel> courseModelOptional = courseRepository.findById(id);

        return courseModelOptional.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/id/{id}")
    public ResponseEntity<ApiResponse> updateCourse(@PathVariable String id, @RequestBody CourseModel courseModel){
        Optional<CourseModel> courseModelOptional = courseRepository.findById(id);

        if(courseModelOptional.isPresent()){
            CourseModel existingCourse = courseModelOptional.get();
            existingCourse.setCourseCode(courseModel.getCourseCode());
            existingCourse.setCourseName(courseModel.getCourseName());
            existingCourse.setCourseDescription(courseModel.getCourseDescription());
            existingCourse.setEnrollmentLimit(courseModel.getEnrollmentLimit());
            existingCourse.setGradingScale(courseModel.getGradingScale());
            existingCourse.setStartDate(courseModel.getStartDate());
            existingCourse.setEndDate(courseModel.getEndDate());
            existingCourse.setStatus(courseModel.getStatus());

            courseRepository.save(existingCourse);
        }

        ApiResponse apiResponse = new ApiResponse("Course details updated successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("update/status/id/{id}")
    public ResponseEntity<ApiResponse> updateStatus(@PathVariable String id, @RequestBody CourseModel courseModel){
        Optional<CourseModel> courseModelOptional = courseRepository.findById(id);

        if(courseModelOptional.isPresent()){
            CourseModel existingCourse = courseModelOptional.get();
            existingCourse.setStatus(courseModel.getStatus());

            courseRepository.save(existingCourse);
        }

        ApiResponse apiResponse = new ApiResponse("Course status updated successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("{courseId}/user/assign")
    public ResponseEntity<ApiResponse> assignUser(@PathVariable String courseId, @RequestBody String requestBody){
        
        ObjectMapper objectMapper = new ObjectMapper();
        String returnMsg = "";
        
        Optional<CourseModel> courseModelOptional = courseRepository.findById(courseId);

        if(courseModelOptional.isPresent()){
            List<Context> users = (List<Context>) courseModelOptional.get().getUsers();
            
            try{
                JsonNode requestBodyJson = objectMapper.readTree(requestBody);

                Context user = new Context();
                user.setVariable("email", requestBodyJson.get("email").asText());
                user.setVariable("role", requestBodyJson.get("role").asText());

                users.add(user);

                courseModelOptional.get().setUsers(users);
                courseRepository.save(courseModelOptional.get());

                returnMsg = "User assigned to the course successfully";
                
            }catch (Exception e){
                returnMsg = "Failed to assign the user to the course";
            }
        }else{
            returnMsg = "Course not found.";
        }

        ApiResponse apiResponse = new ApiResponse(returnMsg);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("{courseId}/users/get")
    public ResponseEntity<Object> getAllCourseUsers(@PathVariable String courseId){
        Optional<CourseModel> courseModelOptional = courseRepository.findById(courseId);
        JSONArray usersList = new JSONArray();

        if (courseModelOptional.isPresent()){
            List<Context> users = (List<Context>) courseModelOptional.get().getUsers();

            for(int i = 0; i < users.size(); i++){
                JSONObject user = new JSONObject();

                user.put("email", users.get(i).getVariable("email"));
                user.put("role", users.get(i).getVariable("role"));

                usersList.put(user);
            }
        }

        return ResponseEntity.ok(usersList.toList());
    }

    @DeleteMapping("/delete/id/{id}")
    public ResponseEntity<ApiResponse> deleteCourse(@PathVariable String id){
        courseRepository.deleteById(id);

        ApiResponse apiResponse = new ApiResponse("Course deleted successfully");
        return ResponseEntity.ok(apiResponse);
    }
}
