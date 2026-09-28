-- =====================================================================
-- Migração do banco para a versão corrigida do app.
-- Rode no Supabase: Dashboard > SQL Editor > New query > Run.
-- =====================================================================

-- 1) "ativo" passa a se chamar "concluida" (o nome agora diz o que significa).
alter table public.tarefas rename column ativo to concluida;
alter table public.tarefas alter column concluida set default false;

-- 2) O dono da tarefa é preenchido pelo próprio banco, a partir do token.
--    O app não envia mais usuario_id, então ninguém consegue forjar esse valor.
alter table public.tarefas alter column usuario_id set default auth.uid();

-- 3) Row Level Security: cada usuário só enxerga e altera as próprias tarefas.
alter table public.tarefas enable row level security;

drop policy if exists "tarefas_select_dono" on public.tarefas;
drop policy if exists "tarefas_insert_dono" on public.tarefas;
drop policy if exists "tarefas_update_dono" on public.tarefas;
drop policy if exists "tarefas_delete_dono" on public.tarefas;

create policy "tarefas_select_dono" on public.tarefas
    for select to authenticated
    using (usuario_id = (select auth.uid()));

create policy "tarefas_insert_dono" on public.tarefas
    for insert to authenticated
    with check (usuario_id = (select auth.uid()));

create policy "tarefas_update_dono" on public.tarefas
    for update to authenticated
    using (usuario_id = (select auth.uid()))
    with check (usuario_id = (select auth.uid()));

create policy "tarefas_delete_dono" on public.tarefas
    for delete to authenticated
    using (usuario_id = (select auth.uid()));

-- Se você já tinha políticas com outros nomes, confira em
-- Authentication > Policies e remova as antigas para não sobrarem regras mais permissivas.
