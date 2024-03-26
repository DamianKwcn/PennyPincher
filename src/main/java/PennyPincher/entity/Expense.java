package PennyPincher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table( name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "expense_id")
    private Integer id;

    @Column(name = "expense_name", nullable = false)
    private String name;

    @Column(name = "expense_cost", nullable = false)
    private BigDecimal totalCost;

    @Column(name = "expenseBalance")
    private BigDecimal expenseBalance;

    @Temporal(TemporalType.DATE)
    @Column(name = "creation_date")
    private LocalDate creationDate;

    @ElementCollection
    @CollectionTable(name = "debt_per_user_mapping",
            joinColumns = @JoinColumn(name = "expense_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "debt_per_user")
    private Map<Integer, BigDecimal> debtPerUser = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "cost_per_user_mapping",
            joinColumns = @JoinColumn(name = "expense_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "cost_per_user")
    private Map<Integer, BigDecimal> costPerUser = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "payoff_per_user_mapping",
            joinColumns = @JoinColumn(name = "expense_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "payoff_per_user")
    private Map<Integer, BigDecimal> payoffPerUser = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "balance_per_user_mapping",
            joinColumns = @JoinColumn(name = "expense_id"))
    @MapKeyColumn(name = "user_id")
    @Column(name = "balance_per_user")
    private Map<Integer, BigDecimal> balancePerUser = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "expense_participants",
            joinColumns = @JoinColumn(name = "expense_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new LinkedList<>();

    @OneToMany(mappedBy = "expensePaid", fetch = FetchType.LAZY,
            cascade = {CascadeType.REMOVE})
    private List<Payoff> payoffs = new ArrayList<>();
}
