package com.furinafans.stusys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 针对已启动服务和当前 MySQL 表结构的真实 HTTP 测试。
 * 运行前须设置 stusys.api.integration=true；每项测试只清理自己创建的记录。
 */
@Tag("integration")
@EnabledIfSystemProperty(named = "stusys.api.integration", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class CurrentSchemaApiTest {
    private static final String BASE_URL = System.getProperty("stusys.api.base-url", "http://127.0.0.1:8080");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    private JdbcTemplate jdbc;

    private String tag;
    private final List<Long> gradeIds = new ArrayList<>();
    private final List<Long> classIds = new ArrayList<>();
    private final List<Long> studentIds = new ArrayList<>();
    private final List<Long> courseIds = new ArrayList<>();
    private final List<Long> enrollmentIds = new ArrayList<>();
    private final List<Long> scoreIds = new ArrayList<>();

    @BeforeAll
    void verifyServerAndSchema() throws Exception {
        // 直接查询新字段，避免旧数据库结构下的测试产生误导性结果。
        jdbc.queryForList("SELECT student_course_id FROM scores LIMIT 0");
        jdbc.queryForList("SELECT term, status FROM student_course LIMIT 0");

        // 先经 JDBC 写入、再经 HTTP 读取，证明测试清理的就是服务所用数据库。
        String marker = "P" + UUID.randomUUID().toString().substring(0, 8);
        try {
            jdbc.update("INSERT INTO grades (name) VALUES (?)", marker);
            Long id = jdbc.queryForObject("SELECT id FROM grades WHERE name = ?", Long.class, marker);
            assertNotNull(id);
            assertEquals(marker, ok("GET", "/grades/" + id, null).path("name").asText());
        } finally {
            jdbc.update("DELETE FROM grades WHERE name = ?", marker);
        }
    }

    @BeforeEach
    void newFixture() {
        tag = UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void actuatorHealth() throws Exception {
        HttpResponse<String> response = send("GET", "/actuator/health", null);
        assertEquals(200, response.statusCode());
        assertEquals("UP", JSON.readTree(response.body()).path("status").asText());
    }

    @Test
    void gradesCrudAndValidation() throws Exception {
        assertCode("POST", "/grades", "{\"name\":\"\"}", 400);
        long id = createGrade();
        assertEquals("G" + tag, ok("GET", "/grades/" + id, null).path("name").asText());
        assertEquals("H" + tag,
                ok("PUT", "/grades/" + id, "{\"name\":\"H" + tag + "\"}").path("name").asText());
        assertPageContains("/grades", id);
        ok("DELETE", "/grades/" + id, null);
        gradeIds.remove(id);
        assertCode("GET", "/grades/" + id, null, 404);
        assertCode("DELETE", "/grades/" + id, null, 404);
        assertCode("GET", "/grades?pageNum=0", null, 400);
    }

    @Test
    void classesCrudAndFiltering() throws Exception {
        long gradeId = createGrade();
        assertCode("POST", "/classes", "{\"name\":\"X\",\"gradeId\":0}", 400);
        long id = createClass(gradeId);
        assertEquals(gradeId, ok("GET", "/classes/" + id, null).path("gradeId").asLong());
        assertEquals("D" + tag, ok("PUT", "/classes/" + id,
                "{\"name\":\"D" + tag + "\",\"gradeId\":" + gradeId + "}")
                .path("name").asText());
        assertRecord(ok("GET", "/classes?gradeId=" + gradeId + "&name=D" + tag, null), id);
        assertEquals(gradeId, jdbc.queryForObject("SELECT grade_id FROM classes WHERE id = ?", Long.class, id));
        ok("DELETE", "/classes/" + id, null);
        classIds.remove(id);
        assertCode("GET", "/classes/" + id, null, 404);
    }

    @Test
    void studentsCrudAndFiltering() throws Exception {
        long gradeId = createGrade();
        long classId = createClass(gradeId);
        assertCode("POST", "/students", studentBody(0, "X" + tag, "Bad"), 400);
        long id = createStudent(classId);
        assertEquals(id, ok("GET", "/students/N" + tag, null).path("id").asLong());
        ok("PUT", "/students/N" + tag, studentBody(classId, "M" + tag, "B" + tag));
        assertEquals(id, ok("GET", "/students/M" + tag, null).path("id").asLong());
        assertRecord(ok("GET", "/students?classId=" + classId + "&gradeId=" + gradeId
                + "&number=M" + tag, null), id);
        assertEquals("M" + tag, jdbc.queryForObject("SELECT number FROM students WHERE id = ?", String.class, id));
        ok("DELETE", "/students/" + id, null);
        studentIds.remove(id);
        assertCode("GET", "/students/M" + tag, null, 404);
    }

    @Test
    void coursesCrudAndFiltering() throws Exception {
        assertCode("POST", "/courses", "{\"name\":\"\",\"code\":\"X\"}", 400);
        long id = createCourse();
        assertEquals(id, ok("GET", "/courses/C" + tag, null).path("id").asLong());
        ok("PUT", "/courses/K" + tag, "{\"name\":\"L" + tag + "\",\"code\":\"Q" + tag + "\"}");
        assertEquals(id, ok("GET", "/courses/Q" + tag, null).path("id").asLong());
        assertRecord(ok("GET", "/courses?name=L" + tag, null), id);
        assertEquals("Q" + tag, jdbc.queryForObject("SELECT code FROM courses WHERE id = ?", String.class, id));
        ok("DELETE", "/courses/" + id, null);
        courseIds.remove(id);
        assertCode("GET", "/courses/Q" + tag, null, 404);
    }

    @Test
    void enrollmentsAndStateTransitions() throws Exception {
        long studentId = createStudent(createClass(createGrade()));
        long courseId = createCourse();
        assertCode("POST", "/student-course", "{\"studentId\":" + studentId + "}", 400);
        String firstBody = enrollmentBody(studentId, courseId, 1);
        long first = createdId("/student-course", firstBody, enrollmentIds);
        assertEquals(1, jdbc.queryForObject("SELECT term FROM student_course WHERE id = ?", Integer.class, first));
        assertRecord(ok("GET", "/student-course?studentId=" + studentId + "&courseId=" + courseId
                + "&academicYear=2098-2099&term=1&status=1", null), first);
        assertCode("POST", "/student-course", firstBody, 409);
        ok("PUT", "/student-course/tuike/" + first, null);
        assertEquals(3, jdbc.queryForObject("SELECT status FROM student_course WHERE id = ?", Integer.class, first));
        assertCode("PUT", "/student-course/tuike/" + first, null, 404);

        long second = createdId("/student-course", enrollmentBody(studentId, courseId, 2), enrollmentIds);
        ok("PUT", "/student-course/jieke/" + second, null);
        assertEquals(2, findRecord(ok("GET", "/student-course?studentId=" + studentId, null), second)
                .path("status").asInt());
        assertCode("PUT", "/student-course/jieke/" + second, null, 409);
    }

    @Test
    void scoresCrudAndFiltering() throws Exception {
        long studentId = createStudent(createClass(createGrade()));
        long courseId = createCourse();
        long enrollmentId = createdId("/student-course", enrollmentBody(studentId, courseId, 1), enrollmentIds);
        assertCode("POST", "/scores", scoreBody(enrollmentId, "-1"), 400);
        long id = createdId("/scores", scoreBody(enrollmentId, "81.50"), scoreIds);
        assertEquals(enrollmentId,
                jdbc.queryForObject("SELECT student_course_id FROM scores WHERE id = ?", Long.class, id));
        assertRecord(ok("GET", "/scores?scId=" + enrollmentId + "&examType=final", null), id);
        ok("PUT", "/scores/" + id, scoreBody(enrollmentId, "92.00"));
        assertEquals(92.0, findRecord(ok("GET", "/scores?scId=" + enrollmentId, null), id)
                .path("score").asDouble());
        assertEquals(0, jdbc.queryForObject("SELECT ABS(score - 92) FROM scores WHERE id = ?", Integer.class, id));
        ok("DELETE", "/scores/" + id, null);
        scoreIds.remove(id);
        assertFalse(hasRecord(ok("GET", "/scores?scId=" + enrollmentId, null), id));
        assertCode("DELETE", "/scores/" + id, null, 404);
        assertCode("GET", "/scores?pageNum=1", null, 400);
    }

    private long createGrade() throws Exception {
        return createdId("/grades", "{\"name\":\"G" + tag + "\"}", gradeIds);
    }

    private long createClass(long gradeId) throws Exception {
        return createdId("/classes", "{\"name\":\"A" + tag + "\",\"gradeId\":" + gradeId + "}", classIds);
    }

    private long createStudent(long classId) throws Exception {
        return createdId("/students", studentBody(classId, "N" + tag, "A" + tag), studentIds);
    }

    private long createCourse() throws Exception {
        return createdId("/courses", "{\"name\":\"K" + tag + "\",\"code\":\"C" + tag + "\"}", courseIds);
    }

    private String studentBody(long classId, String number, String name) {
        return "{\"classId\":" + classId + ",\"number\":\"" + number + "\",\"name\":\"" + name + "\"}";
    }

    private String enrollmentBody(long studentId, long courseId, int term) {
        return "{\"studentId\":" + studentId + ",\"courseId\":" + courseId
                + ",\"academicYear\":\"2098-2099\",\"term\":" + term + "}";
    }

    private String scoreBody(long enrollmentId, String score) {
        return "{\"scId\":" + enrollmentId + ",\"score\":" + score + ",\"examType\":\"final\"}";
    }

    private long createdId(String path, String body, List<Long> ids) throws Exception {
        JsonNode data = ok("POST", path, body);
        assertNotNull(data, path + " 未返回 data");
        long id = data.path("id").asLong();
        assertTrue(id > 0, path + " 未返回有效主键: " + data);
        ids.add(id);
        return id;
    }

    private JsonNode ok(String method, String path, String body) throws Exception {
        JsonNode response = request(method, path, body);
        assertEquals(200, response.path("code").asInt(), method + " " + path + ": " + response);
        return response.get("data");
    }

    private void assertCode(String method, String path, String body, int expected) throws Exception {
        JsonNode response = request(method, path, body);
        assertEquals(expected, response.path("code").asInt(), method + " " + path + ": " + response);
    }

    private JsonNode request(String method, String path, String body) throws Exception {
        HttpResponse<String> response = send(method, path, body);
        assertEquals(200, response.statusCode(), method + " " + path + ": " + response.body());
        return JSON.readTree(response.body());
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(10)).header("Accept", "application/json");
        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json; charset=UTF-8")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        return HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertPageContains(String path, long id) throws Exception {
        // 年级接口无过滤参数；逐页查找，避免数据库已有记录超过单页上限时误报。
        for (int pageNum = 1; ; pageNum++) {
            JsonNode page = ok("GET", path + "?pageNum=" + pageNum + "&pageSize=100", null);
            if (hasRecord(page, id)) return;
            if (pageNum >= page.path("pages").asInt()) {
                throw new AssertionError(path + " 的分页结果未包含 ID=" + id);
            }
        }
    }

    private void assertRecord(JsonNode page, long id) {
        assertTrue(hasRecord(page, id), "分页结果未包含 ID=" + id + ": " + page);
    }

    private boolean hasRecord(JsonNode page, long id) {
        for (JsonNode record : page.path("records")) {
            if (record.path("id").asLong() == id) return true;
        }
        return false;
    }

    private JsonNode findRecord(JsonNode page, long id) {
        for (JsonNode record : page.path("records")) {
            if (record.path("id").asLong() == id) return record;
        }
        throw new AssertionError("分页结果未包含 ID=" + id + ": " + page);
    }

    /** API 没有删除选课接口；按本用例记录的主键逆序清理外键链。 */
    @AfterEach
    void cleanUp() {
        // 即使写入已成功而响应解析失败，也能通过本轮唯一标记找回并清理记录。
        recoverIds("grades", "name", gradeIds, "G" + tag, "H" + tag);
        recoverIds("classes", "name", classIds, "A" + tag, "D" + tag);
        recoverIds("students", "number", studentIds, "N" + tag, "M" + tag);
        recoverIds("courses", "code", courseIds, "C" + tag, "Q" + tag);
        for (long studentId : studentIds) {
            addIds(enrollmentIds, jdbc.queryForList(
                    "SELECT id FROM student_course WHERE student_id = ?", Long.class, studentId));
        }
        for (long enrollmentId : enrollmentIds) {
            addIds(scoreIds, jdbc.queryForList(
                    "SELECT id FROM scores WHERE student_course_id = ?", Long.class, enrollmentId));
        }
        deleteIds("scores", scoreIds);
        deleteIds("student_course", enrollmentIds);
        deleteIds("students", studentIds);
        deleteIds("courses", courseIds);
        deleteIds("classes", classIds);
        deleteIds("grades", gradeIds);
    }

    private void recoverIds(String table, String column, List<Long> ids, String... values) {
        for (String value : values) {
            addIds(ids, jdbc.queryForList("SELECT id FROM " + table + " WHERE " + column + " = ?",
                    Long.class, value));
        }
    }

    private void addIds(List<Long> ids, List<Long> found) {
        for (long id : found) {
            if (!ids.contains(id)) ids.add(id);
        }
    }

    private void deleteIds(String table, List<Long> ids) {
        // 表名仅由调用处的常量提供，ID 参数绑定；不触碰测试前已有记录。
        for (long id : ids) {
            jdbc.update("DELETE FROM " + table + " WHERE id = ?", id);
            assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE id = ?",
                    Integer.class, id));
        }
        ids.clear();
    }
}
