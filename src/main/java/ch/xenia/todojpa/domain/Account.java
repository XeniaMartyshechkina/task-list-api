package ch.xenia.todojpa.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatusEnum status;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(nullable = false, name = "account_id")
    private List<Task> tasks;

    public Account() {
        tasks = new ArrayList<>();
    }

    public Account(String title) {
        this.title = title;
        tasks = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AccountStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AccountStatusEnum status) {
        this.status = status;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        tasks.add(task);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", account title='" + title + '\'' +
                ", status=" + status +
                ", tasks=" + tasks +
                '}';
    }
}
