package vavi.apps.umjammer03.lr;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MixiFeint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;	
    private String mixi;
    private String feint;

    public MixiFeint(String mixi) {
        super();
        setMixi(mixi);
    }

    public MixiFeint(String feint, String mixi) {
        super();
        setFeint(feint);
        setMixi(mixi);
    }

    public String getMixi() {
        return mixi;
    }

    public String getFeint() {
        return feint;
    }

    public void setMixi(String mixi) {
        this.mixi = mixi;
    }

    public void setFeint(String feint) {
        this.feint = feint;
    }

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("id=" + id + ", ");
		sb.append("feint=" + feint + ", ");
		sb.append("mixi=" + mixi);
		return sb.toString();
	}
}
