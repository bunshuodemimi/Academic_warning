package com.warning.warning_system.controller;

import com.warning.warning_system.entity.*;
import com.warning.warning_system.repository.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired private AdministratorRepository adminRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private TeacherRepository teacherRepo;
    @Autowired private CounsellorRepository counsellorRepo;
    @Autowired private CollegeRepository collegeRepo;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private WarningInformationRepository warningRepo;
    @Autowired private RuleRepository ruleRepo;

    @Value("${app.jwt.secret}")
    private String secret;

    // === 鉴权工具 ===
    private void checkAdmin(String token) {
        if (token == null || !token.startsWith("Bearer ")) throw new RuntimeException("未登录");
        try {
            String realToken = token.substring(7);
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            Claims claims = Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .parseClaimsJws(realToken)
                    .getBody();
            if (!"administrator".equals(claims.get("role"))) throw new RuntimeException("权限不足");
        } catch (Exception e) {
            throw new RuntimeException("身份验证失败");
        }
    }

    // === 1. 个人中心 ===
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> params) {
        try {
            checkAdmin(token);
            Integer id = 1; // 默认管理员ID
            if(params.get("id") != null) id = Integer.parseInt(params.get("id"));

            Administrator admin = adminRepo.findById(id).orElseThrow(() -> new RuntimeException("管理员不存在"));
            if (!admin.getPassword().equals(params.get("oldPassword"))) return ResponseEntity.badRequest().body("原密码错误");

            admin.setPassword(params.get("newPassword"));
            adminRepo.save(admin);
            return ResponseEntity.ok("修改成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // === 2. 学生管理 (支持搜索) ===
    @GetMapping("/students")
    public ResponseEntity<?> getStudents(@RequestHeader("Authorization") String token, @RequestParam(required = false) String name) {
        checkAdmin(token);
        if (name != null && !name.isEmpty()) return ResponseEntity.ok(studentRepo.findByStudentNameContaining(name));
        return ResponseEntity.ok(studentRepo.findAll());
    }

    @PostMapping("/students")
    public ResponseEntity<?> addStudent(@RequestHeader("Authorization") String token, @RequestBody Student s) {
        checkAdmin(token);
        s.setPassword("123456");
        studentRepo.save(s);
        return ResponseEntity.ok("添加成功");
    }

    @PutMapping("/students")
    public ResponseEntity<?> updateStudent(@RequestHeader("Authorization") String token, @RequestBody Student s) {
        checkAdmin(token);
        studentRepo.save(s);
        return ResponseEntity.ok("修改成功");
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        checkAdmin(token);
        studentRepo.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    // === 3. 教师管理 (支持搜索) ===
    @GetMapping("/teachers")
    public ResponseEntity<?> getTeachers(@RequestHeader("Authorization") String token, @RequestParam(required = false) String name) {
        checkAdmin(token);
        if (name != null && !name.isEmpty()) return ResponseEntity.ok(teacherRepo.findByTeacherNameContaining(name));
        return ResponseEntity.ok(teacherRepo.findAll());
    }

    @PostMapping("/teachers")
    public ResponseEntity<?> addTeacher(@RequestHeader("Authorization") String token, @RequestBody Teacher t) {
        checkAdmin(token);
        t.setPassword("123456");
        teacherRepo.save(t);
        return ResponseEntity.ok("添加成功");
    }

    @PutMapping("/teachers")
    public ResponseEntity<?> updateTeacher(@RequestHeader("Authorization") String token, @RequestBody Teacher t) {
        checkAdmin(token);
        teacherRepo.save(t);
        return ResponseEntity.ok("修改成功");
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<?> deleteTeacher(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        checkAdmin(token);
        teacherRepo.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    // === 4. 辅导员管理 (支持搜索) ===
    @GetMapping("/counsellors")
    public ResponseEntity<?> getCounsellors(@RequestHeader("Authorization") String token, @RequestParam(required = false) String name) {
        checkAdmin(token);
        if (name != null && !name.isEmpty()) return ResponseEntity.ok(counsellorRepo.findByCounsellorNameContaining(name));
        return ResponseEntity.ok(counsellorRepo.findAll());
    }

    @PostMapping("/counsellors")
    public ResponseEntity<?> addCounsellor(@RequestHeader("Authorization") String token, @RequestBody Counsellor c) {
        checkAdmin(token);
        c.setPassword("123456");
        counsellorRepo.save(c);
        return ResponseEntity.ok("添加成功");
    }

    @PutMapping("/counsellors")
    public ResponseEntity<?> updateCounsellor(@RequestHeader("Authorization") String token, @RequestBody Counsellor c) {
        checkAdmin(token);
        counsellorRepo.save(c);
        return ResponseEntity.ok("修改成功");
    }

    @DeleteMapping("/counsellors/{id}")
    public ResponseEntity<?> deleteCounsellor(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        checkAdmin(token);
        counsellorRepo.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    // === 5. 学院管理 (支持搜索) ===
    @GetMapping("/colleges")
    public ResponseEntity<?> getColleges(@RequestHeader("Authorization") String token, @RequestParam(required = false) String name) {
        checkAdmin(token);
        if (name != null && !name.isEmpty()) return ResponseEntity.ok(collegeRepo.findByCollegeNameContaining(name));
        return ResponseEntity.ok(collegeRepo.findAll());
    }

    @Transactional
    @PostMapping("/colleges")
    public ResponseEntity<?> addCollege(@RequestHeader("Authorization") String token, @RequestBody College c) {
        checkAdmin(token);
        College saved = collegeRepo.save(c);

        Rule rule = new Rule();
        rule.setCollegeName(saved.getCollegeName());
        rule.setAttendence(4);
        rule.setFailure(2);
        ruleRepo.save(rule);

        return ResponseEntity.ok("添加学院成功，已自动生成默认预警规则");
    }

    @PutMapping("/colleges")
    public ResponseEntity<?> updateCollege(@RequestHeader("Authorization") String token, @RequestBody College c) {
        checkAdmin(token);
        collegeRepo.save(c);
        return ResponseEntity.ok("修改成功");
    }

    @DeleteMapping("/colleges/{id}")
    public ResponseEntity<?> deleteCollege(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        checkAdmin(token);
        collegeRepo.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    // === 6. 科目管理 (新增增删改查 + 搜索) ===
    @GetMapping("/subjects")
    public ResponseEntity<?> getSubjects(@RequestHeader("Authorization") String token, @RequestParam(required = false) String name) {
        checkAdmin(token);
        if (name != null && !name.isEmpty()) return ResponseEntity.ok(subjectRepo.findByNameContaining(name));
        return ResponseEntity.ok(subjectRepo.findAll());
    }

    @PostMapping("/subjects")
    public ResponseEntity<?> addSubject(@RequestHeader("Authorization") String token, @RequestBody Subject s) {
        checkAdmin(token);
        // 这里学院前端传的是 ID (collegeId)，但 Subject 表存的是名称 (collegeName)
        // 所以我们要做个转换，或者前端传名称。
        // 为了方便，我们在 AdminDashboard.vue 里会处理好，
        // 这里假设前端传过来的是 subject 对象，其中 collegeName 字段已经被填入 "计算机学院" 这种字符串
        // 如果前端传的是 ID，需要在这里查一次 College 表。
        // 根据之前的代码，Subject 实体有 collegeName 字段。

        // 如果前端传的是数字 ID，我们需要转换
        if (s.getCollegeName() != null && s.getCollegeName().matches("\\d+")) {
            Optional<College> c = collegeRepo.findById(Integer.parseInt(s.getCollegeName()));
            c.ifPresent(value -> s.setCollegeName(value.getCollegeName()));
        }

        subjectRepo.save(s);
        return ResponseEntity.ok("添加成功");
    }

    @PutMapping("/subjects")
    public ResponseEntity<?> updateSubject(@RequestHeader("Authorization") String token, @RequestBody Subject s) {
        checkAdmin(token);
        // 同上，处理学院名称
        if (s.getCollegeName() != null && s.getCollegeName().matches("\\d+")) {
            Optional<College> c = collegeRepo.findById(Integer.parseInt(s.getCollegeName()));
            c.ifPresent(value -> s.setCollegeName(value.getCollegeName()));
        }
        subjectRepo.save(s);
        return ResponseEntity.ok("修改成功");
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        checkAdmin(token);
        subjectRepo.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    // === 7. 预警信息管理 ===
    @GetMapping("/warnings")
    public ResponseEntity<?> getWarnings(@RequestHeader("Authorization") String token, @RequestParam(required = false) String name) {
        checkAdmin(token);
        // 这里沿用原来的 studentName 搜索
        if (name != null && !name.isEmpty()) return ResponseEntity.ok(warningRepo.findByCollegeIdAndStudentNameContaining(null, name)); // 注意：这里需要 Repository 支持忽略 ID 的搜索，或者简单点只 findAll
        // 简单起见，如果 Repository 不支持只搜名字，就 findAll 后在内存过滤，或者你去改 Repository
        // 假设你 WarningInformationRepository 里有 findByStudentNameContaining
        return ResponseEntity.ok(warningRepo.findAll());
    }

    @PutMapping("/warnings")
    public ResponseEntity<?> updateWarning(@RequestHeader("Authorization") String token, @RequestBody WarningInformation w) {
        checkAdmin(token);
        WarningInformation existing = warningRepo.findById(w.getId()).orElseThrow();
        existing.setTitle(w.getTitle());
        existing.setText(w.getText());
        warningRepo.save(existing);
        return ResponseEntity.ok("修改成功");
    }

    @DeleteMapping("/warnings/{id}")
    public ResponseEntity<?> deleteWarning(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        checkAdmin(token);
        warningRepo.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    // === 8. 预警规则管理 ===
    @GetMapping("/rules")
    public ResponseEntity<?> getRules(@RequestHeader("Authorization") String token, @RequestParam(required = false) String name) {
        checkAdmin(token);
        if (name != null && !name.isEmpty()) {
            return ResponseEntity.ok(ruleRepo.findByCollegeNameContaining(name));
        }
        return ResponseEntity.ok(ruleRepo.findAll());
    }

    @PutMapping("/rules")
    public ResponseEntity<?> updateRule(@RequestHeader("Authorization") String token, @RequestBody Rule r) {
        checkAdmin(token);
        ruleRepo.save(r);
        return ResponseEntity.ok("修改成功");
    }

    @PostMapping("/rules")
    public ResponseEntity<?> addRule(@RequestHeader("Authorization") String token, @RequestBody Rule r) {
        checkAdmin(token);
        ruleRepo.save(r);
        return ResponseEntity.ok("添加成功");
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<?> deleteRule(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        checkAdmin(token);
        ruleRepo.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }
}