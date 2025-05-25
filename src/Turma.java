import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Turma {
    private final String codigo;
    private final Professor professor;
    private final String semestre;
    private final String metodoAvaliacao;
    private final boolean presencial;
    private final String sala;
    private final String horario;
    private final int capacidadeMaxima;
    private final int totalAulas;
    private transient Disciplina disciplina;
    private final List<Aluno> alunosMatriculados = new ArrayList<>();

    private final Map<Aluno, Map<String, Double>> notasPorAluno = new HashMap<>();
    private final Map<Aluno, Integer> faltasPorAluno = new HashMap<>();

    public Turma(Disciplina disciplina, String codigo, Professor professor, String semestre,
            String metodoAvaliacao, boolean presencial,
            String sala, String horario, int capacidadeMaxima, int totalAulas) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código da turma inválido");
        }
        this.codigo = codigo;
        this.professor = professor;
        this.semestre = semestre;
        this.metodoAvaliacao = metodoAvaliacao;
        this.presencial = presencial;
        this.sala = presencial ? sala : null;
        this.horario = horario;
        this.capacidadeMaxima = capacidadeMaxima;
        this.totalAulas = totalAulas;
        this.disciplina = disciplina;
    }

    public boolean matricularAluno(Aluno aluno) {
        if (aluno == null || isLotada()) {
            return false;
        }
        alunosMatriculados.add(aluno);
        notasPorAluno.putIfAbsent(aluno, new HashMap<>());
        faltasPorAluno.putIfAbsent(aluno, 0);
        return true;
    }

    public void removerAluno(Aluno aluno) {
        alunosMatriculados.remove(aluno);
        notasPorAluno.remove(aluno);
        faltasPorAluno.remove(aluno);
    }

    public void registrarNota(Aluno aluno, String tipoAvaliacao, double nota) {
        if (!alunosMatriculados.contains(aluno)) {
            throw new IllegalArgumentException("Aluno não está matriculado nesta turma");
        }
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10");
        }
        notasPorAluno.get(aluno).put(tipoAvaliacao, nota);
    }

    public Map<String, Double> getNotas(Aluno aluno) {
        return new HashMap<>(notasPorAluno.getOrDefault(aluno, new HashMap<>()));
    }

    public void registrarFalta(Aluno aluno) {
        if (!alunosMatriculados.contains(aluno)) {
            throw new IllegalArgumentException("Aluno não está matriculado nesta turma");
        }
        faltasPorAluno.put(aluno, faltasPorAluno.getOrDefault(aluno, 0) + 1);
    }

    public int getFaltas(Aluno aluno) {
        return faltasPorAluno.getOrDefault(aluno, 0);
    }

    public String getMetodoAvaliacao() {
        return metodoAvaliacao;
    }

    public double calcularMedia(Aluno aluno) {
        Map<String, Double> notas = getNotas(aluno);
        if (notas.isEmpty())
            return 0.0;

        if (this.metodoAvaliacao.equals("Método 1")) {
            return (notas.getOrDefault("P1", 0.0) +
                    notas.getOrDefault("P2", 0.0) +
                    notas.getOrDefault("P3", 0.0) +
                    notas.getOrDefault("L", 0.0) +
                    notas.getOrDefault("S", 0.0)) / 5;
        } else {
            return (notas.getOrDefault("P1", 0.0) +
                    notas.getOrDefault("P2", 0.0) * 2 +
                    notas.getOrDefault("P3", 0.0) * 3 +
                    notas.getOrDefault("L", 0.0) +
                    notas.getOrDefault("S", 0.0)) / 8;
        }
    }

    public double calcularFrequencia(Aluno aluno) {
        if (totalAulas == 0)
            return 0.0;
        int presencas = totalAulas - getFaltas(aluno);
        return (double) presencas / totalAulas * 100;
    }

    public String verificarAprovacao(Aluno aluno) {
        double media = calcularMedia(aluno);
        double frequencia = calcularFrequencia(aluno);

        if (frequencia < 75)
            return "Reprovado por falta";
        return media >= 5 ? "Aprovado" : "Reprovado por nota";
    }

    public String getCodigo() {
        return codigo;
    }

    public Professor getProfessor() {
        return professor;
    }

    public String getSemestre() {
        return semestre;
    }

    public boolean isPresencial() {
        return presencial;
    }

    public String getSala() {
        return sala;
    }

    public String getHorario() {
        return horario;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public int getTotalAulas() {
        return totalAulas;
    }

    public List<Aluno> getAlunosMatriculados() {
        return new ArrayList<>(alunosMatriculados);
    }

    public int getVagasDisponiveis() {
        return capacidadeMaxima - alunosMatriculados.size();
    }

    public boolean isLotada() {
        return alunosMatriculados.size() >= capacidadeMaxima;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public String getInfo() {
        return String.format(
                "%s | %s | %s | %s | %s | %d/%d vagas",
                codigo,
                professor.getNome(),
                horario,
                presencial ? "Presencial" : "Remoto",
                presencial ? sala : "N/A",
                alunosMatriculados.size(),
                capacidadeMaxima);
    }

    @Override
    public String toString() {
        return String.format("Turma[%s, %s, %s]", codigo, horario, professor.getNome());
    }
}
