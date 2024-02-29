package PennyPincher.repository;

import PennyPincher.entity.Payoff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoffRepository extends JpaRepository<Payoff, Integer> {
}
