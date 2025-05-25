public class Aluno {

    private String nome;
    private final String matricula; // matricula nao pode ser alterada dps
    private String curso;
    private boolean especial;

    public Aluno(String nome, String matricula, String curso, boolean especial) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matricula invalida!");
        }
        this.nome = nome;
        this.matricula = matricula;
        this.curso = curso;
        this.especial = especial;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public boolean isEspecial() {
        return especial;
    }

    public void setEspecial(boolean especial) {
        this.especial = especial;
    }

    public boolean podeMatricular(int cursosAtuais) {
        return true;
    }

    private boolean emAfastamento;

    public void setEmAfastamento(boolean emAfastamento) {
        this.emAfastamento = emAfastamento;
    }

    public boolean isEmAfastamento() {
        return emAfastamento;
    }

    @Override
    public String toString() {
        return "Aluno: " + nome + "\n" +
                "Matrícula: " + matricula + "\n" +
                "Curso: " + curso + "\n" +
                "Especial: " + (especial ? "Sim" : "Não") + "\n" +
                "Em afastamento: " + (emAfastamento ? "Sim" : "Não") + "\n";
    }

}
