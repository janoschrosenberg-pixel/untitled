package lisp.sample;

import java.util.*;
import java.util.stream.Collectors;

import static lisp.sample.SimpleSECD.Machine.deepCopyEnv;

public class SimpleSECD {

    // ------------------- Werte -------------------
    abstract static class Value { }

    static class IntVal extends Value {
        final int value;
        IntVal(int v) { this.value = v; }
        public String toString() { return String.valueOf(value); }
    }

    static class BoolVal extends Value {
        final boolean value;
        BoolVal(boolean v) { this.value = v; }
        public String toString() { return String.valueOf(value); }
    }

    // Closure enthält Parameter, Rumpf und die Umgebung zum Zeitpunkt der Erstellung
    static class Closure extends Value {
        final String param;
        final List<Instr> body;
        final List<Map<String, Value>> env;
        Closure(String p, List<Instr> b, List<Map<String, Value>> e) {
            this.param = p;
            this.body = b;
            this.env = deepCopyEnv(e);
        }
    }

    // ------------------- Instruktionen -------------------
    abstract static class Instr { }

    static class LDC extends Instr { Value val; LDC(Value v) { this.val = v; } }
    static class LD  extends Instr { String var; LD(String v) { this.var = v; } }
    static class LDF extends Instr { String param; List<Instr> body; LDF(String p, List<Instr> b) { this.param = p; this.body = b; } }
    static class AP  extends Instr { }
    static class RTN extends Instr { }
    static class ADD extends Instr { }
    static class SUB extends Instr { }
    static class MUL extends Instr { }
    static class EQ  extends Instr { }
    static class SEL extends Instr { List<Instr> thenB, elseB; SEL(List<Instr> t, List<Instr> e) { this.thenB = t; this.elseB = e; } }
    static class JOIN extends Instr { }

    // ------------------- Die SECD-Maschine -------------------
    static class Machine {
        Deque<Value> S = new ArrayDeque<>();                    // Stack
        List<Map<String, Value>> E = new ArrayList<>();         // Environment
        Deque<Instr> C = new ArrayDeque<>();                    // Control
        Deque<DumpEntry> D = new ArrayDeque<>();                // Dump

        record DumpEntry(Deque<Value> s, List<Map<String, Value>> e, Deque<Instr> c) {}

        void run(List<Instr> program) {
            C.addAll(program);
            E.add(new HashMap<>());  // globales Frame

            while (!C.isEmpty()) {
                Instr i = C.removeFirst();

                if (i instanceof LDC ldc) {
                    S.push(ldc.val);

                } else if (i instanceof LD ld) {
                    Value val = lookup(E, ld.var);
                    if (val == null) throw new RuntimeException("Unbound variable: " + ld.var);
                    S.push(val);

                } else if (i instanceof LDF ldf) {
                    S.push(new Closure(ldf.param, ldf.body, E));

                } else if (i instanceof AP) {
                    Closure clos = (Closure) S.pop();   // Funktion
                    Value arg    = S.pop();             // Argument

                    // Zustand sichern
                    D.push(new DumpEntry(new ArrayDeque<>(S), deepCopyEnv(E), new ArrayDeque<>(C)));

                    S.clear();                          // Stack leeren!

                    // Neue Umgebung mit gebundenem Parameter
                    Map<String, Value> frame = new HashMap<>();
                    frame.put(clos.param, arg);
                    E = new ArrayList<>(clos.env);
                    E.add(frame);

                    C = new ArrayDeque<>(clos.body);

                } else if (i instanceof RTN) {
                    Value result = S.pop();

                    if (D.isEmpty()) {
                        System.out.println("Ergebnis: " + result);
                        return;
                    }

                    DumpEntry saved = D.pop();
                    S = new ArrayDeque<>(saved.s);
                    E = saved.e;
                    C = new ArrayDeque<>(saved.c);
                    S.push(result);

                } else if (i instanceof ADD) {
                    IntVal b = (IntVal) S.pop();
                    IntVal a = (IntVal) S.pop();
                    S.push(new IntVal(a.value + b.value));

                } else if (i instanceof SUB) {
                    IntVal b = (IntVal) S.pop();
                    IntVal a = (IntVal) S.pop();
                    S.push(new IntVal(a.value - b.value));

                } else if (i instanceof MUL) {
                    IntVal b = (IntVal) S.pop();
                    IntVal a = (IntVal) S.pop();
                    S.push(new IntVal(a.value * b.value));

                } else if (i instanceof EQ) {
                    IntVal b = (IntVal) S.pop();
                    IntVal a = (IntVal) S.pop();
                    S.push(new BoolVal(a.value == b.value));

                } else if (i instanceof SEL sel) {
                    BoolVal cond = (BoolVal) S.pop();

                    D.push(new DumpEntry(new ArrayDeque<>(S), deepCopyEnv(E), new ArrayDeque<>(C)));
                    S.clear();

                    C = new ArrayDeque<>(cond.value ? sel.thenB : sel.elseB);
                    C.addLast(new JOIN());

                } else if (i instanceof JOIN) {
                    Value res = S.pop();
                    DumpEntry saved = D.pop();
                    S = new ArrayDeque<>(saved.s);
                    E = saved.e;
                    C = new ArrayDeque<>(saved.c);
                    S.push(res);
                }
            }

            // Falls Programm ohne RTN endet (z. B. nur Konstante)
            if (!S.isEmpty()) {
                System.out.println("Ergebnis: " + S.peek());
            }
        }

        private Value lookup(List<Map<String, Value>> env, String var) {
            for (int i = env.size() - 1; i >= 0; i--) {
                Value v = env.get(i).get(var);
                if (v != null) return v;
            }
            return null;
        }

        static List<Map<String, Value>> deepCopyEnv(List<Map<String, Value>> env) {
            return env.stream()
                    .map(m -> new HashMap<>(m))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    // ------------------- Rekursive Fakultät -------------------
    private static List<Instr> createFactBody() {
        List<Instr> body = new ArrayList<>();

        List<Instr> thenBranch = List.of(
                new LDC(new IntVal(1)),
                new JOIN()
        );

        List<Instr> elseBranch = new ArrayList<>();
        elseBranch.add(new LD("n"));                    // n (für Multiplikation)
        elseBranch.add(new LD("n"));                    // n
        elseBranch.add(new LDC(new IntVal(1)));
        elseBranch.add(new SUB());                      // n-1
        elseBranch.add(new LDF("n", body));             // rekursive Closure
        elseBranch.add(new AP());                       // fact(n-1)
        elseBranch.add(new MUL());                      // n * fact(n-1)
        elseBranch.add(new JOIN());

        body.add(new LD("n"));
        body.add(new LDC(new IntVal(0)));
        body.add(new EQ());
        body.add(new SEL(thenBranch, elseBranch));
        body.add(new RTN());  // WICHTIG: Rückgabe der Funktion!

        return body;
    }

    // ------------------- Hauptprogramm -------------------
    public static void main(String[] args) {
        List<Instr> factBody = createFactBody();

        List<Instr> program = List.of(
                new LDC(new IntVal(10)),        // ändere hier z. B. auf 10, 0, 1 ...
                new LDF("n", factBody),
                new AP()
        );

        System.out.println("Berechne 6! mit SECD-Maschine...");
        new Machine().run(program);
    }
}