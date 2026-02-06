package alchemy.model.pets.moves;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "MoveLoadout")
public class MoveLoadout {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	private UUID id;

	@ManyToMany
    @OrderColumn(name = "priority_order") // Hibernate gère une colonne d'index automatique
    @JoinTable(
        name = "loadout_moves",
        joinColumns = @JoinColumn(name = "loadout_id"),
        inverseJoinColumns = @JoinColumn(name = "move_id")
    )
    private List<Move> moves = new ArrayList<>();

    public void addMove(Move move) {
        this.moves.add(move);
    }

    public void setMovePriority(int currentIndex, int newIndex) {
        Move move = moves.remove(currentIndex);
        moves.add(newIndex, move);
    }

}
