package com.example.plagiarismchecker.config;

import com.example.plagiarismchecker.entity.Assignment;
import com.example.plagiarismchecker.entity.Role;
import com.example.plagiarismchecker.entity.Submission;
import com.example.plagiarismchecker.entity.User;
import com.example.plagiarismchecker.repository.AssignmentRepository;
import com.example.plagiarismchecker.repository.SubmissionRepository;
import com.example.plagiarismchecker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Populates the database with demo data (2 teachers, 5 students, 2
 * assignments, several submissions - some intentionally similar) the first
 * time the application starts against an empty database. This lets a
 * grader/teacher try the whole workflow immediately without manual setup.
 *
 * Set app.seed.enabled=false in application.properties to skip this.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEMO_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Database already has data - skipping demo seed.");
            return;
        }

        log.info("Seeding demo data...");

        User teacher1 = saveUser("Dr. Anjali Mehta", "teacher1@college.edu", Role.TEACHER);
        User teacher2 = saveUser("Prof. Rakesh Kulkarni", "teacher2@college.edu", Role.TEACHER);

        User student1 = saveUser("Rahul Sharma", "student1@college.edu", Role.STUDENT);
        User student2 = saveUser("Amit Verma", "student2@college.edu", Role.STUDENT);
        User student3 = saveUser("Priya Nair", "student3@college.edu", Role.STUDENT);
        User student4 = saveUser("Neha Joshi", "student4@college.edu", Role.STUDENT);
        User student5 = saveUser("John David", "student5@college.edu", Role.STUDENT);

        Assignment assignment1 = assignmentRepository.save(Assignment.builder()
                .title("Introduction to Object-Oriented Programming")
                .description("Write a short paragraph (4-6 sentences) explaining what object-oriented "
                        + "programming is and why Java is considered an object-oriented language.")
                .deadline(LocalDateTime.now().plusDays(14))
                .teacher(teacher1)
                .build());

        Assignment assignment2 = assignmentRepository.save(Assignment.builder()
                .title("Databases: SQL vs NoSQL")
                .description("Compare relational (SQL) databases with NoSQL databases. Discuss at least "
                        + "two advantages and two disadvantages of each.")
                .deadline(LocalDateTime.now().plusDays(21))
                .teacher(teacher2)
                .build());

        // Assignment 1 submissions - student1 and student2 are intentionally
        // paraphrased/similar; student3 and student4 are a second similar
        // pair on a different subject; student5 is clearly different.
        saveSubmission(assignment1, student1,
                "Java is an object oriented programming language used to develop applications. "
                        + "It organizes software design around objects rather than functions and logic. "
                        + "An object contains data in the form of fields and code in the form of methods. "
                        + "Java supports concepts like inheritance, encapsulation, and polymorphism, which make "
                        + "programs easier to maintain and reuse. This is why Java is widely used in enterprise "
                        + "applications.");

        saveSubmission(assignment1, student2,
                "Java is an object oriented language commonly used for developing applications. "
                        + "It structures software around objects instead of functions and logic. "
                        + "An object stores data as fields and behavior as methods. "
                        + "Java also supports inheritance, encapsulation, and polymorphism, making "
                        + "programs simpler to maintain and reuse. That is why Java is popular for enterprise "
                        + "software.");

        saveSubmission(assignment1, student3,
                "Object oriented programming is a way of writing software using objects that combine "
                        + "state and behavior. Java is one of the most popular object oriented languages because "
                        + "it runs on the Java Virtual Machine, is platform independent, and has strong community "
                        + "support along with a huge standard library used across the industry.");

        saveSubmission(assignment1, student4,
                "In simple words, object oriented programming lets us model real world things as objects "
                        + "inside a program. Java became a leading choice for this style of programming since "
                        + "it is platform independent through the JVM, has excellent community support, and "
                        + "ships with a very large standard library that developers rely on daily.");

        saveSubmission(assignment1, student5,
                "Cloud computing allows businesses to rent computing power, storage, and services over the "
                        + "internet instead of owning physical servers. This reduces upfront hardware cost, "
                        + "improves scalability during traffic spikes, and enables teams to deploy applications "
                        + "faster using managed platforms offered by providers.");

        // Assignment 2 submissions - student1 and student3 are similar;
        // student2 and student5 are different from everyone else.
        saveSubmission(assignment2, student1,
                "SQL databases store data in tables with fixed rows and columns and use structured query "
                        + "language for queries. NoSQL databases store data in flexible formats like documents "
                        + "or key value pairs. SQL is strong for complex transactions, while NoSQL scales more "
                        + "easily across many servers but can be harder to keep fully consistent.");

        saveSubmission(assignment2, student3,
                "SQL databases keep data in tables made of fixed rows and columns and rely on structured "
                        + "query language to fetch data. NoSQL databases keep data in flexible formats such as "
                        + "documents or key value pairs. SQL is great for complex transactions, whereas NoSQL "
                        + "scales more easily across many servers but can be harder to keep fully consistent.");

        saveSubmission(assignment2, student2,
                "A relational database organizes information into related tables, enforcing a strict schema "
                        + "that guarantees data integrity through constraints and foreign keys, which is ideal "
                        + "for banking or inventory systems. Document databases, on the other hand, are schema "
                        + "flexible and better suited to rapidly changing data models such as user profiles.");

        saveSubmission(assignment2, student5,
                "Choosing between SQL and NoSQL often comes down to the shape of your data and how you plan "
                        + "to query it. Highly relational data with many joins usually fits SQL better, while "
                        + "large volumes of semi structured data that need to scale horizontally often fit a "
                        + "NoSQL solution better.");

        log.info("Demo data seeded successfully.");
        log.info("Demo login credentials (password for all accounts: {})", DEMO_PASSWORD);
        log.info("  Teacher: teacher1@college.edu / {}", DEMO_PASSWORD);
        log.info("  Teacher: teacher2@college.edu / {}", DEMO_PASSWORD);
        log.info("  Student: student1@college.edu / {}", DEMO_PASSWORD);
        log.info("  Student: student2@college.edu / {}", DEMO_PASSWORD);
        log.info("  Student: student3@college.edu / {}", DEMO_PASSWORD);
        log.info("  Student: student4@college.edu / {}", DEMO_PASSWORD);
        log.info("  Student: student5@college.edu / {}", DEMO_PASSWORD);
    }

    private User saveUser(String name, String email, Role role) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .role(role)
                .build());
    }

    private void saveSubmission(Assignment assignment, User student, String content) {
        submissionRepository.save(Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(content)
                .build());
    }
}
