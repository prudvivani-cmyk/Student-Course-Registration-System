import dao.RegistrationDAO;
import dao.CourseDAO;
import models.Registration;
import models.Course;
import java.util.List;

public class TestUpdate {
    public static void main(String[] args) {
        RegistrationDAO regDao = new RegistrationDAO();
        CourseDAO courseDao = new CourseDAO();
        
        List<Course> courses = courseDao.getAllCourses();
        System.out.println("Courses: " + courses.size());
        
        // Find a student
        List<Object[]> summary = regDao.getStudentSummary();
        if (summary.isEmpty()) {
            System.out.println("No students registered.");
            return;
        }
        String sid = (String) summary.get(0)[0];
        System.out.println("Student ID: " + sid);
        
        List<Registration> regs = regDao.getStudentRegistrations(sid);
        if (regs.isEmpty()) {
            System.out.println("Student has no regs.");
            return;
        }
        Registration reg = regs.get(0);
        String oldId = reg.getCourseId();
        System.out.println("Old Course ID: " + oldId);
        
        String newId = null;
        for (Course c : courses) {
            String cId = c.getCourseId();
            boolean found = false;
            for (Registration r : regs) {
                if (r.getCourseId().equals(cId)) found = true;
            }
            if (!found) {
                newId = cId;
                break;
            }
        }
        
        if (newId == null) {
            System.out.println("No available new course to test.");
            return;
        }
        
        System.out.println("Trying to update to: " + newId);
        boolean success = regDao.updateRegistrationCourse(sid, oldId, newId);
        System.out.println("Update success: " + success);
    }
}
