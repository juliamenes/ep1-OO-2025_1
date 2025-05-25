public class AlunoEspecial extends Aluno {
    private static final int MAX_DISCIPLINAS = 2;

    public AlunoEspecial(String nome, String matricula, String curso) {
        super(nome, matricula, curso, true); 
    }

    @Override
    public boolean podeMatricular(int cursosAtuais) {
        return cursosAtuais < MAX_DISCIPLINAS;
    }

    @Override
    public String toString() {
        return super.toString() + " [ESTUDANTE ESPECIAL - MAX. " + MAX_DISCIPLINAS + " DISCIPLINAS]";
    }
}
