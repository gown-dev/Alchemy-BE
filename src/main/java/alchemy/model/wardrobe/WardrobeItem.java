package alchemy.model.wardrobe;

import java.util.UUID;
import alchemy.model.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "WardrobeItem")
public class WardrobeItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
	private UUID id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "author", nullable = false)
	private Account author;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "approver", nullable = true)
	private Account approver;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "price")
	private int price;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "category")
	private WardrobeItemCategory category;
	
	@Column(name = "frontImage")
	private String frontImage;
	
	@Column(name = "backImage")
	private String backImage;
	
	@Column(name = "removed")
	private boolean removed;

}
