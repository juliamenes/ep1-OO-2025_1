public class AlunoCsvFormatter implements CsvService.CsvFormatter<Aluno> {
    @Override
    public String getHeader() {
        return "nome,matricula,curso,especial,emAfastamento";
    }

    @Override
    public String format(Aluno aluno) {
        return String.format("\"%s\",%s,\"%s\",%s,%s",
                aluno.getNome().replace("\"", "\"\""),
                aluno.getMatricula(),
                aluno.getCurso().replace("\"", "\"\""),
                aluno.isEspecial(),
                aluno.isEmAfastamento());
    }
}