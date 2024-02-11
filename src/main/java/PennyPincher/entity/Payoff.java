package PennyPincher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table( name = "payoff")
public class Payoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payoff_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expensePaid;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userPaying;

    @Column(name = "payoff_amount", nullable = false)
    private BigDecimal payoffAmount;

    @Override
    public String toString() {
        return "Payoff{" +
                "expensePaid= " + expensePaid.getName() +
                ", userPaying= " + userPaying.getFirstName() +
                ", payoffAmount=" + payoffAmount +
                '}';
    }
}
