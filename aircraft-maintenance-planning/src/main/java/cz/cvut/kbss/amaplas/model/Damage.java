package cz.cvut.kbss.amaplas.model;


import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

@OWLClass(iri = Vocabulary.s_c_damage)
public class Damage extends AbstractEntityWithDescription {

    @OWLObjectProperty(iri = Vocabulary.s_p_has_type)
    protected DamageType damageType;

    @OWLObjectProperty(iri = Vocabulary.s_p_damage_of)
    protected Component component;

    public DamageType getDamageType() {
        return damageType;
    }

    public void setDamageType(DamageType damageType) {
        this.damageType = damageType;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}
