import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Disciplina {
    private final String codigo;
    private final String nome;
    private final int cargaHoraria;
    private final List<String> prerequisitos;
    private final List<Turma> turmas = new ArrayList<>();

    public Disciplina(String codigo, String nome, int cargaHoraria, List<String> prerequisitos) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código da disciplina inválido");
        }
        this.codigo = codigo;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.prerequisitos = new ArrayList<>(prerequisitos);
    }

    /**
     * 
     * @param novaTurma
     * @return
     */
    public boolean adicionarTurma(Turma novaTurma) {
        Objects.requireNonNull(novaTurma, "Turma não pode ser nula");

        boolean conflitoHorario = turmas.stream()
                .anyMatch(t -> t.getHorario().equals(novaTurma.getHorario()));

        if (!conflitoHorario) {
            turmas.add(novaTurma);
            return true;
        }
        return false;
    }

    /**
     * 
     * @param turma
     */
    public void removerTurma(Turma turma) {
        turmas.remove(turma);
    }

    /**
     * 
     * @param aluno
     * @param disciplinasConcluidas
     * @return
     */
    public boolean alunoPodeMatricular(Aluno aluno, List<String> disciplinasConcluidas) {

        if (!disciplinasConcluidas.containsAll(prerequisitos)) {
            System.out.printf("Pré-requisitos faltantes: %s%n",
                    prerequisitos.stream()
                            .filter(p -> !disciplinasConcluidas.contains(p))
                            .collect(Collectors.joining(", ")));
            return false;
        }

        if (aluno instanceof AlunoEspecial) {
            long matriculasAtuais = turmas.stream()
                    .flatMap(t -> t.getAlunosMatriculados().stream())
                    .filter(a -> a.equals(aluno))
                    .count();
            if (matriculasAtuais >= 2) {
                System.out.println("Aluno especial atingiu o limite de 2 disciplinas");
                return false;
            }
        }
        return true;
    }

    /**
     * 
     * @return
     */
    public List<Turma> getTurmasComVagas() {
        return turmas.stream()
                .filter(t -> t.getVagasDisponiveis() > 0)
                .collect(Collectors.toList());
    }

    public boolean temVagasDisponiveis() {
        return turmas.stream().anyMatch(t -> t.getVagasDisponiveis() > 0);
    }

    // getters

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    /**
     * @return
     */
    public List<String> getPrerequisitos() {
        return Collections.unmodifiableList(prerequisitos);
    }

    /**
     * @return
     */
    public List<Turma> getTurmas() {
        return Collections.unmodifiableList(turmas);
    }

    /**
     * @return
     */
    public int getTotalMatriculados() {
        return turmas.stream()
                .mapToInt(t -> t.getAlunosMatriculados().size())
                .sum();
    }

    /**
     * @return
     */
    public String getInfo() {
        return String.format("%s (%s) - %dh | %d turmas | %d alunos",
                nome,
                codigo,
                cargaHoraria,
                turmas.size(),
                getTotalMatriculados());
    }

    @Override
    public String toString() {
        return String.format("Disciplina[%s, %s]", codigo, nome);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Disciplina that = (Disciplina) o;
        return codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}