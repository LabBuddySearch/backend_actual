package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "submissions")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Column(nullable = false, length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private int executionTimeMs;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "submission")
    private List<SecurityLog> logs;

}
