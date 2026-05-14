import PendingUserList from '../components/admin/PendingUserList';
import UserList from '../components/admin/UserList';

export default function AdminPage() {
  return (
    <div data-testid="admin-page">
      <h1>사용자 관리</h1>
      <section style={{ marginBottom: 32 }}>
        <h2>신규 가입자 (승인 대기)</h2>
        <PendingUserList />
      </section>
      <section>
        <h2>전체 사용자</h2>
        <UserList />
      </section>
    </div>
  );
}
