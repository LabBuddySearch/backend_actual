package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer timeLimitMs;

    private Integer memoryLimitMb;

    private LocalDateTime deadlineAt;

    @Builder.Default
    private Integer maxAttempts = 3;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskCategory category = TaskCategory.ALGORITHMS;

    /** Видна всем студентам из активных групп автора-задачи. */
    @Builder.Default
    @Column(nullable = false)
    private Boolean assignToAllTeacherGroups = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_group_id")
    private Group assignedGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_student_id")
    private User assignedStudent;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();

    @OneToMany(mappedBy = "task")
    private List<Submission> submissions;

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
        testCase.setTask(this);
    }
}
