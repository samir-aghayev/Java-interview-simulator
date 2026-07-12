import { t } from '../i18n/strings';
import type { Paged } from '../types';

export default function Pager<T>({
  data,
  onPage
}: {
  data: Paged<T>;
  onPage: (page: number) => void;
}) {
  if (data.totalPages <= 1) return null;
  return (
    <div className="pager">
      <button className="btn-small" disabled={data.page === 0} onClick={() => onPage(data.page - 1)}>
        {t.prevPage}
      </button>
      <span>{t.pageInfo(data.page + 1, data.totalPages, data.totalElements)}</span>
      <button
        className="btn-small"
        disabled={data.page >= data.totalPages - 1}
        onClick={() => onPage(data.page + 1)}
      >
        {t.nextPage}
      </button>
    </div>
  );
}
