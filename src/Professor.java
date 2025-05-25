import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Professor {
    private final String matricula; // professores sao identificados por uma matricula
    private String nome;
    private String departamento;
    private final List<Turma> turmasMinistradas = new ArrayList<>();

    /**
     * 
     * @param matricula
     * @param nome
     * @param departamento
     */
    public Professor(String matricula, String nome, String departamento) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula inválida");
        }
        this.matricula = matricula;
        this.nome = nome;
        this.departamento = departamento;
    }

    /**
     * 
     * @param turma
     */
    public void adicionarTurma(Turma turma) {
        if (turma != null && !turmasMinistradas.contains(turma)) {
            turmasMinistradas.add(turma);
        }
    }

    /**
     * 
     * @param turma
     */
    public void removerTurma(Turma turma) {
        turmasMinistradas.remove(turma);
    }

    // getters e setters

    public String getMatricula() {
        return matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome;
        }
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    /**
     * @return
     */
    public List<Turma> getTurmasMinistradas() {
        return new ArrayList<>(turmasMinistradas);
    }

    /**
     * @return
     */
    public int getCargaHoraria() {
        return turmasMinistradas.size();
    }

    /**
     * @return
     */
    public String getInfo() {
        return String.format("%s (%s) - %s | %d turmas",
                nome,
                matricula,
                departamento,
                turmasMinistradas.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Professor professor = (Professor) o;
        return matricula.equals(professor.matricula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricula);
    }

    @Override
    public String toString() {
        return String.format("Professor[%s, %s]", nome, matricula);
    }
}
