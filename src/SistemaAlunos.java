import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class SistemaAlunos {
    private final List<Aluno> alunos = new ArrayList<>();
    private final List<Disciplina> disciplinas = new ArrayList<>();
    private final List<Professor> professores = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);

    public void loadAllFromCsv() {
        alunos.clear();
        disciplinas.clear();
        professores.clear();

        try {

            new File("data").mkdirs();

            loadProfessoresFromCsv();

            loadDisciplinasFromCsv();

            loadAlunosFromCsv();

            loadTurmasFromCsv();

            System.out.println("Dados carregados automaticamente do CSV!");
        } catch (IOException e) {
            System.out.println("Nenhum dado anterior encontrado. Iniciando novo sistema.");
        }
    }

    private void loadProfessoresFromCsv() throws IOException {
        File file = new File("data/professores.csv");
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 3) {
                    professores.add(new Professor(parts[0], parts[1], parts[2]));
                }
            }
        }
    }

    private void loadDisciplinasFromCsv() throws IOException {
        File file = new File("data/disciplinas.csv");
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 4) {
                    List<String> prereqs = parts[3].isEmpty() ? new ArrayList<>() : Arrays.asList(parts[3].split(";"));
                    disciplinas.add(new Disciplina(parts[0], parts[1], Integer.parseInt(parts[2]), prereqs));
                }
            }
        }
    }

    private void loadAlunosFromCsv() throws IOException {
        File file = new File("data/alunos.csv");
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 5) {
                    Aluno aluno;
                    if (parts[3].equalsIgnoreCase("true")) {
                        aluno = new AlunoEspecial(parts[0], parts[1], parts[2]);
                    } else {
                        aluno = new Aluno(parts[0], parts[1], parts[2], false);
                    }
                    aluno.setEmAfastamento(Boolean.parseBoolean(parts[4]));
                    alunos.add(aluno);
                }
            }
        }
    }

    private void loadTurmasFromCsv() throws IOException {
        File file = new File("data/turmas.csv");
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length >= 10) {
                    Disciplina disciplina = buscarDisciplinaPorCodigo(parts[1]);
                    Professor professor = buscarProfessor(parts[2]);

                    if (disciplina != null && professor != null) {
                        Turma turma = new Turma(
                                disciplina,
                                parts[0],
                                professor,
                                parts[3],
                                parts[4],
                                Boolean.parseBoolean(parts[5]),
                                parts[6].isEmpty() ? null : parts[6],
                                parts[7],
                                Integer.parseInt(parts[8]),
                                Integer.parseInt(parts[9]));

                        // Load enrolled students
                        if (parts.length >= 11 && !parts[10].isEmpty()) {
                            Arrays.stream(parts[10].split(";"))
                                    .map(this::buscarAlunoPorMatricula)
                                    .filter(Objects::nonNull)
                                    .forEach(turma::matricularAluno);
                        }

                        // Load grades if the field exists
                        if (parts.length >= 12 && !parts[11].isEmpty()) {
                            Arrays.stream(parts[11].split(";"))
                                    .filter(s -> !s.isEmpty())
                                    .forEach(gradeData -> {
                                        String[] alunoGradeParts = gradeData.split(":");
                                        if (alunoGradeParts.length == 2) {
                                            Aluno aluno = buscarAlunoPorMatricula(alunoGradeParts[0]);
                                            if (aluno != null) {
                                                Arrays.stream(alunoGradeParts[1].split(","))
                                                        .forEach(gradeEntry -> {
                                                            String[] gradeParts = gradeEntry.split("=");
                                                            if (gradeParts.length == 2) {
                                                                try {
                                                                    double nota = Double.parseDouble(gradeParts[1]);
                                                                    turma.registrarNota(aluno, gradeParts[0], nota);
                                                                } catch (NumberFormatException e) {
                                                                    System.err.println(
                                                                            "Formato de nota inválido: " + gradeEntry);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }

                        // Load absences if the field exists
                        if (parts.length >= 13 && !parts[12].isEmpty()) {
                            Arrays.stream(parts[12].split(";"))
                                    .filter(s -> !s.isEmpty())
                                    .forEach(faltaData -> {
                                        String[] faltaParts = faltaData.split("=");
                                        if (faltaParts.length == 2) {
                                            Aluno aluno = buscarAlunoPorMatricula(faltaParts[0]);
                                            if (aluno != null) {
                                                try {
                                                    int faltas = Integer.parseInt(faltaParts[1]);
                                                    for (int i = 0; i < faltas; i++) {
                                                        turma.registrarFalta(aluno);
                                                    }
                                                } catch (NumberFormatException e) {
                                                    System.err.println("Formato de falta inválido: " + faltaData);
                                                }
                                            }
                                        }
                                    });
                        }

                        disciplina.adicionarTurma(turma);
                        professor.adicionarTurma(turma);
                    }
                }
            }
        }
    }

    // Helper method to parse CSV lines with quoted fields
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String part : parts) {
            values.add(part.startsWith("\"") && part.endsWith("\"")
                    ? part.substring(1, part.length() - 1)
                    : part);
        }
        return values.toArray(String[]::new);
    }

    // MENU PRINCIPAL
    public void exibirMenu() {
        while (true) {
            System.out.println("\n=== SISTEMA ACADÊMICO ===");
            System.out.println("1. Gerenciar Alunos");
            System.out.println("2. Gerenciar Disciplinas");
            System.out.println("3. Gerenciar Professores");
            System.out.println("4. Gerenciar Matrículas");
            System.out.println("5. Gerenciar Notas e Frequência");
            System.out.println("6. Gerar Relatórios");
            System.out.println("7. Salvar e Sair");
            System.out.print("Opção: ");

            try {
                int opcao = Integer.parseInt(scanner.nextLine());
                switch (opcao) {
                    case 1 -> menuAlunos();
                    case 2 -> menuDisciplinas();
                    case 3 -> menuProfessores();
                    case 4 -> menuMatriculas();
                    case 5 -> menuNotasFaltas();
                    case 6 -> gerarRelatorios();
                    case 7 -> {
                        saveAllToCsv();
                        System.out.println("Saindo do sistema...");
                        return;
                    }
                    default -> System.out.println("Opção inválida!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido!");
            }
        }
    }

    private void menuAlunos() {
        System.out.println("\n--- GERENCIAR ALUNOS ---");
        System.out.println("1. Cadastrar aluno");
        System.out.println("2. Editar aluno");
        System.out.println("3. Listar alunos");
        System.out.println("4. Voltar");

        switch (scanner.nextLine()) {
            case "1" -> cadastrarAluno();
            case "2" -> editarAluno();
            case "3" -> listarAlunos();
        }
    }

    private void cadastrarAluno() {
        System.out.println("\n--- CADASTRO DE ALUNO ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        String matricula;
        do {
            System.out.print("Matrícula: ");
            matricula = scanner.nextLine();
            if (isMatriculaDuplicada(matricula)) {
                System.out.println("Erro: Matrícula já existe!");
            }
        } while (isMatriculaDuplicada(matricula));

        System.out.print("Curso: ");
        String curso = scanner.nextLine();

        System.out.print("É estudante especial? (S/N): ");
        boolean especial = scanner.nextLine().equalsIgnoreCase("S");

        Aluno aluno = especial ? new AlunoEspecial(nome, matricula, curso) : new Aluno(nome, matricula, curso, false);

        alunos.add(aluno);
        System.out.println("Aluno cadastrado com sucesso!");
    }

    private void editarAluno() {
        System.out.print("Matrícula do aluno: ");
        Aluno aluno = buscarAlunoPorMatricula(scanner.nextLine());

        if (aluno != null) {
            System.out.println("Dados atuais: " + aluno);

            System.out.print("Novo nome (enter para manter): ");
            String nome = scanner.nextLine();
            if (!nome.isBlank())
                aluno.setNome(nome);

            System.out.print("Novo curso (enter para manter): ");
            String curso = scanner.nextLine();
            if (!curso.isBlank())
                aluno.setCurso(curso);

            System.out.println("Dados atualizados: " + aluno);
        } else {
            System.out.println("Aluno não encontrado!");
        }
    }

    private void criarTurma() {
        System.out.println("\n--- CRIAR TURMA ---");

        System.out.println("Disciplinas disponíveis:");
        disciplinas.forEach(d -> System.out.println(d.getCodigo() + " - " + d.getNome()));

        System.out.print("Código da disciplina: ");
        Disciplina disciplina = buscarDisciplinaPorCodigo(scanner.nextLine());

        if (disciplina == null) {
            System.out.println("Disciplina não encontrada!");
            return;
        }

        System.out.println("Professores disponíveis:");
        professores.forEach(p -> System.out.println(p.getMatricula() + " - " + p.getNome()));

        System.out.print("Matrícula do professor: ");
        Professor professor = buscarProfessor(scanner.nextLine());

        if (professor == null) {
            System.out.println("Professor não encontrado!");
            return;
        }

        System.out.print("Código da turma (ex: T01): ");
        String codigoTurma = scanner.nextLine();

        System.out.print("Semestre (ex: 2024.1): ");
        String semestre = scanner.nextLine();

        System.out.print("Método de avaliação: ");
        String metodoAvaliacao = scanner.nextLine();

        System.out.print("Presencial? (S/N): ");
        boolean presencial = scanner.nextLine().equalsIgnoreCase("S");

        String sala = null;
        if (presencial) {
            System.out.print("Sala: ");
            sala = scanner.nextLine();
        }

        System.out.print("Horário (ex: Seg 14h-16h): ");
        String horario = scanner.nextLine();

        System.out.print("Capacidade máxima: ");
        int capacidade = Integer.parseInt(scanner.nextLine());

        System.out.print("Total de aulas no semestre: ");
        int totalAulas = Integer.parseInt(scanner.nextLine());

        Turma novaTurma = new Turma(
                disciplina,
                codigoTurma,
                professor,
                semestre,
                metodoAvaliacao,
                presencial,
                sala,
                horario,
                capacidade,
                totalAulas);

        if (disciplina.adicionarTurma(novaTurma)) {
            professor.adicionarTurma(novaTurma);
            System.out.println("Turma criada com sucesso!");
        } else {
            System.out.println("Erro: Conflito de horário ou turma já existe!");
        }
    }

    private void menuDisciplinas() {
        System.out.println("\n--- GERENCIAR DISCIPLINAS ---");
        System.out.println("1. Cadastrar disciplina");
        System.out.println("2. Criar turma");
        System.out.println("3. Listar disciplinas");
        System.out.println("4. Voltar");

        switch (scanner.nextLine()) {
            case "1" -> cadastrarDisciplina();
            case "2" -> criarTurma();
            case "3" -> listarDisciplinas();
        }
    }

    private void cadastrarDisciplina() {
        System.out.println("\n--- CADASTRO DE DISCIPLINA ---");
        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Carga horária: ");
        int cargaHoraria = Integer.parseInt(scanner.nextLine());

        System.out.print("Pré-requisitos (separados por vírgula): ");
        List<String> prereqs = Arrays.stream(scanner.nextLine().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        disciplinas.add(new Disciplina(codigo, nome, cargaHoraria, prereqs));
        System.out.println("Disciplina cadastrada!");
    }

    private void menuProfessores() {
        System.out.println("\n--- GERENCIAR PROFESSORES ---");
        System.out.println("1. Cadastrar professor");
        System.out.println("2. Listar professores");
        System.out.println("3. Voltar");

        switch (scanner.nextLine()) {
            case "1" -> cadastrarProfessor();
            case "2" -> listarProfessores();
        }
    }

    private void cadastrarProfessor() {
        System.out.println("\n--- CADASTRO DE PROFESSOR ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("Matrícula: ");
        String matricula = scanner.nextLine();

        System.out.print("Departamento: ");
        String departamento = scanner.nextLine();

        professores.add(new Professor(matricula, nome, departamento));
        System.out.println("Professor cadastrado!");
    }

    private void menuMatriculas() {
        System.out.println("\n--- GERENCIAR MATRÍCULAS ---");
        System.out.println("1. Matricular aluno");
        System.out.println("2. Trancar disciplina");
        System.out.println("3. Trancar semestre");
        System.out.println("4. Voltar");

        switch (scanner.nextLine()) {
            case "1" -> matricularAluno();
            case "2" -> trancarDisciplina();
            case "3" -> trancarSemestre();
        }
    }

    private void trancarDisciplina() {
        System.out.print("Matrícula do aluno: ");
        Aluno aluno = buscarAlunoPorMatricula(scanner.nextLine());

        if (aluno == null) {
            System.out.println("Aluno não encontrado!");
            return;
        }

        System.out.print("Código da disciplina: ");
        Disciplina disciplina = buscarDisciplinaPorCodigo(scanner.nextLine());

        if (disciplina == null) {
            System.out.println("Disciplina não encontrada!");
            return;
        }

        List<Turma> turmasMatriculadas = disciplina.getTurmas().stream()
                .filter(t -> t.getAlunosMatriculados().contains(aluno))
                .collect(Collectors.toList());

        if (turmasMatriculadas.isEmpty()) {
            System.out.println("Aluno não está matriculado nesta disciplina!");
            return;
        }

        System.out.print("Confirmar trancamento? (S/N): ");
        boolean confirmar = scanner.nextLine().equalsIgnoreCase("S");

        if (confirmar) {
            turmasMatriculadas.forEach(t -> t.removerAluno(aluno));
            System.out.println("Disciplina trancada com sucesso!");
        }
    }

    private void trancarSemestre() {
        System.out.print("Matrícula do aluno: ");
        Aluno aluno = buscarAlunoPorMatricula(scanner.nextLine());

        if (aluno != null) {
            if (aluno.isEmAfastamento()) {
                System.out.print("Aluno já está com o semestre trancado. Reativar? (S/N): ");
                boolean reativar = scanner.nextLine().equalsIgnoreCase("N");
                aluno.setEmAfastamento(!reativar);
                System.out.println(reativar ? "Semestre reativado!" : "Permanece trancado.");
            } else {
                disciplinas.stream()
                        .flatMap(d -> d.getTurmas().stream())
                        .filter(t -> t.getAlunosMatriculados().contains(aluno))
                        .forEach(t -> t.removerAluno(aluno));

                aluno.setEmAfastamento(true);
                System.out.println("Semestre trancado! Aluno removido de todas as disciplinas.");
            }
        } else {
            System.out.println("Aluno não encontrado!");
        }
    }

    private void matricularAluno() {
        while (true) {
            System.out.print("\nMatrícula do aluno (ou '0' para sair): ");
            String matricula = scanner.nextLine().trim();

            if (matricula.equals("0")) {
                return;
            }

            Aluno aluno = buscarAlunoPorMatricula(matricula);

            if (aluno == null) {
                System.out.println("Aluno não encontrado!");
                continue;
            }

            if (aluno.isEmAfastamento()) {
                System.out.println("Aluno com semestre trancado!");
                continue;
            }

            while (true) {
                System.out.print("\nCódigo da disciplina (ou '0' para voltar): ");
                String codigoDisciplina = scanner.nextLine().trim();

                if (codigoDisciplina.equals("0")) {
                    break;
                }

                Disciplina disciplina = buscarDisciplinaPorCodigo(codigoDisciplina);

                if (disciplina == null) {
                    System.out.println("Disciplina não encontrada!");
                    continue;
                }

                if (!disciplina.getPrerequisitos().isEmpty()) {
                    System.out.println("\nPré-requisitos necessários:");
                    disciplina.getPrerequisitos().forEach(preReq -> System.out.println("- " + preReq));

                    if (!verificarPreRequisitos(aluno, disciplina)) {
                        System.out.println("\nALUNO NÃO ATENDE AOS PRÉ-REQUISITOS!");
                        System.out.println("1. Tentar outra disciplina");
                        System.out.println("2. Voltar ao menu principal");
                        System.out.print("Opção: ");

                        String opcao = scanner.nextLine().trim();
                        if (opcao.equals("2")) {
                            return;
                        }
                        continue;
                    }
                }

                System.out.println("\nTurmas disponíveis:");
                if (disciplina.getTurmas().isEmpty()) {
                    System.out.println("Nenhuma turma disponível para esta disciplina!");
                    continue;
                }

                disciplina.getTurmas().forEach(t -> System.out.printf("%s - %s (%d vagas)\n",
                        t.getCodigo(),
                        t.getHorario(),
                        t.getVagasDisponiveis()));

                System.out.print("\nCódigo da turma (ou '0' para voltar): ");
                String codigoTurma = scanner.nextLine().trim();

                if (codigoTurma.equals("0")) {
                    continue;
                }

                Turma turma = disciplina.getTurmas().stream()
                        .filter(t -> t.getCodigo().equalsIgnoreCase(codigoTurma))
                        .findFirst()
                        .orElse(null);

                if (turma == null) {
                    System.out.println("Turma não encontrada!");
                    continue;
                }

                if (turma.matricularAluno(aluno)) {
                    System.out.println("\n Matrícula realizada com sucesso!");
                    System.out.printf("Aluno: %s\nTurma: %s\nDisciplina: %s\n",
                            aluno.getNome(),
                            turma.getCodigo(),
                            disciplina.getNome());
                    return;
                } else {
                    System.out.println("Turma lotada! Por favor, selecione outra.");
                }
            }
        }
    }

    private boolean verificarPreRequisitos(Aluno aluno, Disciplina disciplina) {
        return disciplina.getPrerequisitos().stream()
                .allMatch(preReq -> {
                    return disciplinas.stream()
                            .filter(d -> d.getCodigo().equals(preReq))
                            .flatMap(d -> d.getTurmas().stream())
                            .anyMatch(t -> t.getAlunosMatriculados().contains(aluno) &&
                                    t.verificarAprovacao(aluno).equals("Aprovado"));
                });
    }

    private boolean isMatriculaDuplicada(String matricula) {
        return alunos.stream().anyMatch(a -> a.getMatricula().equals(matricula));
    }

    private Aluno buscarAlunoPorMatricula(String matricula) {
        return alunos.stream()
                .filter(a -> a.getMatricula().equals(matricula))
                .findFirst()
                .orElse(null);
    }

    private Disciplina buscarDisciplinaPorCodigo(String codigo) {
        return disciplinas.stream()
                .filter(d -> d.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    private Professor buscarProfessor(String matricula) {
        return professores.stream()
                .filter(p -> p.getMatricula().equals(matricula))
                .findFirst()
                .orElse(null);
    }

    private void listarAlunos() {
        System.out.println("\n--- LISTA DE ALUNOS ---");
        alunos.forEach(System.out::println);
        System.out.println();
    }

    private void listarDisciplinas() {
        System.out.println("\n--- LISTA DE DISCIPLINAS ---");
        disciplinas.forEach(d -> System.out.println(d.getInfo()));
        System.out.println();
    }

    private void listarProfessores() {
        System.out.println("\n--- LISTA DE PROFESSORES ---");
        professores.forEach(p -> System.out.println(p.getInfo()));
        System.out.println();
    }

    private void menuNotasFaltas() {
        System.out.println("\n--- GERENCIAR NOTAS E FALTAS ---");
        System.out.println("1. Registrar notas");
        System.out.println("2. Registrar faltas");
        System.out.println("3. Voltar");
        System.out.print("Opção: ");

        String opcao = scanner.nextLine();
        switch (opcao) {
            case "1" -> registrarNotas();
            case "2" -> registrarFaltas();
        }
    }

    private void registrarNotas() {
        System.out.println("\n--- REGISTRAR NOTAS ---");

        System.out.print("Matrícula do aluno: ");
        Aluno aluno = buscarAlunoPorMatricula(scanner.nextLine());
        if (aluno == null) {
            System.out.println("Aluno não encontrado!");
            return;
        }

        System.out.print("Código da disciplina: ");
        Disciplina disciplina = buscarDisciplinaPorCodigo(scanner.nextLine());
        if (disciplina == null) {
            System.out.println("Disciplina não encontrada!");
            return;
        }

        System.out.println("Turmas disponíveis:");
        disciplina.getTurmas().forEach(t -> System.out.println(t.getCodigo() + " - " + t.getHorario()));

        System.out.print("Código da turma: ");
        Turma turma = disciplina.getTurmas().stream()
                .filter(t -> t.getCodigo().equals(scanner.nextLine()))
                .findFirst()
                .orElse(null);

        if (turma == null) {
            System.out.println("Turma não encontrada!");
            return;
        }

        if (!turma.getAlunosMatriculados().contains(aluno)) {
            System.out.println("Aluno não está matriculado nesta turma!");
            return;
        }
        boolean continuar = true;
        while (continuar) {
            System.out.println("\nNotas atuais do aluno:");
            Map<String, Double> notas = turma.getNotas(aluno);
            if (notas.isEmpty()) {
                System.out.println("Nenhuma nota registrada ainda.");
            } else {
                notas.forEach((tipo, nota) -> System.out.printf("- %s: %.1f\n", tipo, nota));
            }

            System.out.print("Tipo de avaliação (P1/P2/P3/Trabalho/etc): ");
            String tipo = scanner.nextLine();

            System.out.print("Nota (0-10): ");
            try {
                double nota = Double.parseDouble(scanner.nextLine());
                if (nota < 0 || nota > 10) {
                    System.out.println("Nota deve estar entre 0 e 10!");
                } else {
                    turma.registrarNota(aluno, tipo, nota);
                    System.out.println("Nota registrada com sucesso!");

                    System.out.print("\nDeseja registrar outra nota? (S/N): ");
                    String resposta = scanner.nextLine();
                    continuar = resposta.equalsIgnoreCase("S");
                }
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido para nota!");

                System.out.print("Deseja tentar novamente? (S/N): ");
                String resposta = scanner.nextLine();
                continuar = resposta.equalsIgnoreCase("S");
            }
        }

        System.out.println("\nVoltando ao menu anterior...");
    }

    private void registrarFaltas() {
        System.out.println("\n--- REGISTRAR FALTAS ---");

        System.out.print("Matrícula do aluno: ");
        Aluno aluno = buscarAlunoPorMatricula(scanner.nextLine());
        if (aluno == null) {
            System.out.println("Aluno não encontrado!");
            return;
        }

        System.out.print("Código da disciplina: ");
        Disciplina disciplina = buscarDisciplinaPorCodigo(scanner.nextLine());
        if (disciplina == null) {
            System.out.println("Disciplina não encontrada!");
            return;
        }

        System.out.println("Turmas disponíveis:");
        disciplina.getTurmas().forEach(t -> System.out.println(t.getCodigo() + " - " + t.getHorario()));

        System.out.print("Código da turma: ");
        Turma turma = disciplina.getTurmas().stream()
                .filter(t -> t.getCodigo().equals(scanner.nextLine()))
                .findFirst()
                .orElse(null);

        if (turma == null) {
            System.out.println("Turma não encontrada!");
            return;
        }

        if (!turma.getAlunosMatriculados().contains(aluno)) {
            System.out.println("Aluno não está matriculado nesta turma!");
            return;
        }

        int faltasAtuais = turma.getFaltas(aluno);
        System.out.printf("\nSituação atual: %d faltas de %d aulas (%.1f%% de frequência)\n",
                faltasAtuais,
                turma.getTotalAulas(),
                turma.calcularFrequencia(aluno));

        System.out.print("\nQuantas faltas deseja registrar? (1, 2, 3, etc.): ");
        try {
            int quantidade = Integer.parseInt(scanner.nextLine());
            if (quantidade <= 0) {
                System.out.println("Quantidade inválida! Deve ser maior que zero.");
                return;
            }

            int novasFaltas = faltasAtuais + quantidade;
            int maxFaltasPermitidas = (int) Math.ceil(turma.getTotalAulas() * 0.25);

            if (novasFaltas > turma.getTotalAulas()) {
                System.out.println("Aviso: O aluno não pode ter mais faltas que o total de aulas!");
                System.out.printf("Total de aulas: %d | Faltas após registro: %d\n",
                        turma.getTotalAulas(), novasFaltas);
                System.out.print("Deseja continuar mesmo assim? (S/N): ");
                String confirmacao = scanner.nextLine();
                if (!confirmacao.equalsIgnoreCase("S")) {
                    return;
                }
            } else if (novasFaltas > maxFaltasPermitidas) {
                System.out.printf("Aviso: O aluno ultrapassará o limite de faltas (máximo %d faltas permitidas)!\n",
                        maxFaltasPermitidas);
                System.out.printf("Faltas após registro: %d | Frequência: %.1f%%\n",
                        novasFaltas,
                        100 - ((novasFaltas * 100.0) / turma.getTotalAulas()));
                System.out.print("Deseja continuar mesmo assim? (S/N): ");
                String confirmacao = scanner.nextLine();
                if (!confirmacao.equalsIgnoreCase("S")) {
                    return;
                }
            }

            for (int i = 0; i < quantidade; i++) {
                turma.registrarFalta(aluno);
            }

            System.out.printf("\n%d faltas registradas com sucesso!\n", quantidade);
            System.out.printf("Situação atualizada: %d faltas de %d aulas (%.1f%% de frequência)\n",
                    turma.getFaltas(aluno),
                    turma.getTotalAulas(),
                    turma.calcularFrequencia(aluno));

            if (turma.calcularFrequencia(aluno) < 75) {
                System.out.println("ATENÇÃO: Aluno agora está REPROVADO POR FALTA!");
            }
        } catch (NumberFormatException e) {
            System.out.println("Quantidade inválida! Digite um número.");
        }
    }

    public void gerarRelatorios() {
        System.out.println("\n--- RELATÓRIOS ---");
        System.out.println("1. Por turma");
        System.out.println("2. Por disciplina");
        System.out.println("3. Por professor");
        System.out.println("4. Por aluno");
        System.out.print("Opção: ");

        switch (scanner.nextLine()) {
            case "1" -> relatorioPorTurma();
            case "2" -> relatorioPorDisciplina();
            case "3" -> relatorioPorProfessor();
            case "4" -> relatorioPorAluno();
        }
    }

    private void relatorioPorTurma() {
        System.out.print("Código da turma: ");
        String codigoTurma = scanner.nextLine();

        Optional<Turma> turmaEncontrada = disciplinas.stream()
                .flatMap(d -> d.getTurmas().stream())
                .filter(t -> t.getCodigo().equalsIgnoreCase(codigoTurma.trim()))
                .findFirst();

        if (turmaEncontrada.isPresent()) {
            Turma turma = turmaEncontrada.get();
            System.out.printf("\n=== RELATÓRIO DA TURMA %s ===\n", turma.getCodigo());
            System.out.printf("Disciplina: %s\n", turma.getDisciplina().getNome());
            System.out.printf("Professor: %s\n", turma.getProfessor().getNome());
            System.out.printf("Horário: %s | %s\n", turma.getHorario(),
                    turma.isPresencial() ? "Presencial" : "Remoto");
            System.out.printf("Total de alunos: %d\n\n", turma.getAlunosMatriculados().size());

            if (turma.getAlunosMatriculados().isEmpty()) {
                System.out.println("Nenhum aluno matriculado nesta turma.");
            } else {
                System.out.println("ALUNOS MATRICULADOS:");
                System.out.println("--------------------------------------------------");
                System.out.printf("%-30s %-8s %-12s %s\n", "Nome", "Média", "Frequência", "Situação");
                System.out.println("--------------------------------------------------");

                turma.getAlunosMatriculados().forEach(aluno -> {
                    System.out.printf("%-30s %-8.1f %-12.1f%% %s\n",
                            aluno.getNome(),
                            turma.calcularMedia(aluno),
                            turma.calcularFrequencia(aluno),
                            turma.verificarAprovacao(aluno));
                });

                double mediaTurma = turma.getAlunosMatriculados().stream()
                        .mapToDouble(turma::calcularMedia)
                        .average()
                        .orElse(0.0);

                double freqMedia = turma.getAlunosMatriculados().stream()
                        .mapToDouble(turma::calcularFrequencia)
                        .average()
                        .orElse(0.0);

                long aprovados = turma.getAlunosMatriculados().stream()
                        .filter(a -> turma.verificarAprovacao(a).equals("Aprovado"))
                        .count();

                System.out.println("--------------------------------------------------");
                System.out.printf("RESUMO: Média da turma: %.1f | Frequência média: %.1f%% | Aprovados: %d/%d\n",
                        mediaTurma, freqMedia, aprovados, turma.getAlunosMatriculados().size());
            }
        } else {
            System.out.println("\nTurma não encontrada! Verifique o código digitado.");
            System.out.println("Turmas disponíveis:");
            disciplinas.stream()
                    .flatMap(d -> d.getTurmas().stream())
                    .forEach(t -> System.out.printf("- %s (%s - %s)\n",
                            t.getCodigo(),
                            t.getDisciplina().getNome(),
                            t.getHorario()));
        }
    }

    private void relatorioPorAluno() {
        System.out.print("Matrícula do aluno: ");
        Aluno aluno = buscarAlunoPorMatricula(scanner.nextLine());

        if (aluno == null) {
            System.out.println("Aluno não encontrado!");
            return;
        }

        System.out.println("\n1. Relatório simplificado");
        System.out.println("2. Relatório completo");
        System.out.print("Opção: ");

        boolean completo = scanner.nextLine().equals("2");

        System.out.printf("\nRelatório do Aluno: %s\n", aluno.getNome());
        disciplinas.stream()
                .flatMap(d -> d.getTurmas().stream())
                .filter(t -> t.getAlunosMatriculados().contains(aluno))
                .forEach(turma -> {
                    System.out.printf("\nDisciplina: %s\n", turma.getDisciplina().getNome());
                    if (completo) {
                        System.out.printf("Professor: %s\n", turma.getProfessor().getNome());
                        System.out.printf("Modalidade: %s\n", turma.isPresencial() ? "Presencial" : "Remoto");
                        System.out.printf("Carga horária: %dh\n", turma.getDisciplina().getCargaHoraria());
                        System.out.printf("Método de avaliação: %s\n", turma.getMetodoAvaliacao());

                        Map<String, Double> notas = turma.getNotas(aluno);
                        if (!notas.isEmpty()) {
                            System.out.println("\nNotas individuais:");
                            notas.forEach((tipo, nota) -> System.out.printf("- %s: %.1f\n", tipo, nota));
                        }
                    }

                    double media = turma.calcularMedia(aluno);
                    double frequencia = turma.calcularFrequencia(aluno);
                    String situacao = turma.verificarAprovacao(aluno);

                    System.out.printf("\nMédia: %.1f - Frequência: %.1f%% - %s\n",
                            media,
                            frequencia,
                            situacao);

                    if (turma.getNotas(aluno).isEmpty()) {
                        System.out.println("AVISO: Nenhuma nota registrada para esta disciplina!");
                    }
                });
        System.out.println();
    }

    private void relatorioPorDisciplina() {
        System.out.print("Código da disciplina: ");
        String codigo = scanner.nextLine();
        Disciplina disciplina = buscarDisciplinaPorCodigo(codigo);

        if (disciplina != null) {
            System.out.printf("\nRELATÓRIO DA DISCIPLINA: %s (%s)\n",
                    disciplina.getNome(), disciplina.getCodigo());
            System.out.println("Carga horária: " + disciplina.getCargaHoraria() + "h");
            System.out.println("Pré-requisitos: " + String.join(", ", disciplina.getPrerequisitos()));
            System.out.println("\nTURMAS OFERECIDAS:");

            disciplina.getTurmas().forEach(turma -> {
                System.out.println("\nTurma: " + turma.getCodigo());
                System.out.println("Professor: " + turma.getProfessor().getNome());
                System.out.println("Horário: " + turma.getHorario());
                System.out.println("Vagas: " + turma.getVagasDisponiveis() + "/" + turma.getCapacidadeMaxima());
                System.out.println("Método avaliação: " + turma.getMetodoAvaliacao());

                System.out.println("\nDESEMPENHO DOS ALUNOS:");
                turma.getAlunosMatriculados().forEach(aluno -> {
                    System.out.printf("- %s (%s): Média %.1f | Frequência %.1f%% | %s\n",
                            aluno.getNome(),
                            aluno.getMatricula(),
                            turma.calcularMedia(aluno),
                            turma.calcularFrequencia(aluno),
                            turma.verificarAprovacao(aluno));
                });

                double mediaTurma = turma.getAlunosMatriculados().stream()
                        .mapToDouble(turma::calcularMedia)
                        .average()
                        .orElse(0.0);

                double freqMedia = turma.getAlunosMatriculados().stream()
                        .mapToDouble(turma::calcularFrequencia)
                        .average()
                        .orElse(0.0);

                long aprovados = turma.getAlunosMatriculados().stream()
                        .filter(a -> turma.verificarAprovacao(a).equals("Aprovado"))
                        .count();

                System.out.printf("\nRESUMO: Média da turma: %.1f | Frequência média: %.1f%% | Aprovados: %d/%d\n\n",
                        mediaTurma, freqMedia, aprovados, turma.getAlunosMatriculados().size());
            });
        } else {
            System.out.println("Disciplina não encontrada!");
        }
    }

    private void relatorioPorProfessor() {
        System.out.print("Matrícula do professor: ");
        String matricula = scanner.nextLine();
        Professor professor = buscarProfessor(matricula);

        if (professor != null) {
            System.out.printf("\nRELATÓRIO DO PROFESSOR: %s (%s)\n",
                    professor.getNome(), professor.getMatricula());
            System.out.println("Departamento: " + professor.getDepartamento());
            System.out.println("Total de turmas: " + professor.getTurmasMinistradas().size());
            System.out.println("----------------------------------");

            professor.getTurmasMinistradas().forEach(turma -> {
                Disciplina disciplina = turma.getDisciplina();

                System.out.printf("\nDISCIPLINA: %s (%s)\n",
                        disciplina.getNome(), disciplina.getCodigo());
                System.out.println("Turma: " + turma.getCodigo());
                System.out.println("Horário: " + turma.getHorario());
                System.out.println("Modalidade: " + (turma.isPresencial() ? "Presencial" : "Remoto"));

                long totalAlunos = turma.getAlunosMatriculados().size();
                long aprovados = turma.getAlunosMatriculados().stream()
                        .filter(a -> turma.verificarAprovacao(a).equals("Aprovado"))
                        .count();

                double mediaTurma = turma.getAlunosMatriculados().stream()
                        .mapToDouble(turma::calcularMedia)
                        .average()
                        .orElse(0.0);

                System.out.printf("\nDESEMPENHO: %d alunos | %d aprovados (%.1f%%) | Média da turma: %.1f\n",
                        totalAlunos, aprovados,
                        totalAlunos > 0 ? (aprovados * 100.0 / totalAlunos) : 0,
                        mediaTurma);

                List<Aluno> alunosAtencao = turma.getAlunosMatriculados().stream()
                        .filter(a -> {
                            String status = turma.verificarAprovacao(a);
                            return !status.equals("Aprovado");
                        })
                        .collect(Collectors.toList());

                if (!alunosAtencao.isEmpty()) {
                    System.out.println("\nALUNOS QUE NECESSITAM ATENÇÃO:");
                    alunosAtencao.forEach(a -> {
                        System.out.printf("- %s (%s): %s | Média %.1f | Frequência %.1f%%\n",
                                a.getNome(), a.getMatricula(),
                                turma.verificarAprovacao(a),
                                turma.calcularMedia(a),
                                turma.calcularFrequencia(a));
                    });
                }
            });
        } else {
            System.out.println("Professor não encontrado!");
        }
    }

    public void saveAllToCsv() {
        try {
            CsvService.saveToCsv("alunos.csv", alunos, new AlunoCsvFormatter());
            CsvService.saveToCsv("disciplinas.csv", disciplinas, new DisciplinaCsvFormatter());
            CsvService.saveToCsv("professores.csv", professores, new ProfessorCsvFormatter());

            saveTurmasToCsv();

            System.out.println("Dados salvos em CSV com sucesso!");
        } catch (IOException e) {
            System.out.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    private void saveTurmasToCsv() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data/turmas.csv"))) {
            writer.println(
                    "codigo,disciplinaCodigo,professorMatricula,semestre,metodoAvaliacao,presencial,sala,horario,capacidadeMaxima,totalAulas,alunosMatriculados,notas,faltas");

            for (Disciplina disciplina : disciplinas) {
                for (Turma turma : disciplina.getTurmas()) {
                    String alunosStr = turma.getAlunosMatriculados().stream()
                            .map(Aluno::getMatricula)
                            .collect(Collectors.joining(";"));

                    String notasStr = turma.getAlunosMatriculados().stream()
                            .map(aluno -> {
                                Map<String, Double> notas = turma.getNotas(aluno);
                                if (notas.isEmpty())
                                    return "";
                                return aluno.getMatricula() + ":" +
                                        notas.entrySet().stream()
                                                .map(e -> e.getKey() + "=" + e.getValue())
                                                .collect(Collectors.joining(","));
                            })
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining(";"));

                    String faltasStr = turma.getAlunosMatriculados().stream()
                            .map(aluno -> aluno.getMatricula() + "=" + turma.getFaltas(aluno))
                            .collect(Collectors.joining(";"));

                    writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%d,%d,%s,%s,%s",
                            turma.getCodigo(),
                            disciplina.getCodigo(),
                            turma.getProfessor().getMatricula(),
                            turma.getSemestre(),
                            turma.getMetodoAvaliacao(),
                            turma.isPresencial(),
                            turma.getSala() != null ? turma.getSala() : "",
                            turma.getHorario(),
                            turma.getCapacidadeMaxima(),
                            turma.getTotalAulas(),
                            alunosStr,
                            notasStr,
                            faltasStr));
                }
            }
        }
    }

    public static void main(String[] args) {
        SistemaAlunos sistema = new SistemaAlunos();
        sistema.loadAllFromCsv();
        sistema.exibirMenu();
    }
}
