import { FormEvent, useState } from "react";
import { useAuthSession } from "../../auth/session/AuthSessionContext";
import {
  MonthlyStatement,
  generateMonthlyStatement,
  listGeneratedMonthlyStatements,
} from "../api/statements";
import { isSessionRecoveryRequired, mapStatementApiError } from "../api/errorMapper";

export function StatementsPage() {
  const { session, signOut } = useAuthSession();
  const accessToken = session?.accessToken ?? "";
  const hasAccessToken = accessToken.trim().length > 0;

  const [accountId, setAccountId] = useState("");
  const [month, setMonth] = useState("");

  const [statement, setStatement] = useState<MonthlyStatement | null>(null);
  const [statementHistory, setStatementHistory] = useState<MonthlyStatement[]>([]);

  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const [isGenerating, setIsGenerating] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isDownloadingPdf, setIsDownloadingPdf] = useState(false);

  type StatementAction = "generate" | "retrieve";

  async function handleGenerate(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!accountId.trim() || !month.trim()) {
      setError("Provide both account ID and month before generating statement.");
      return;
    }

    setIsGenerating(true);
    try {
      const generated = await generateMonthlyStatement(accessToken, accountId.trim(), month.trim());
      setStatement(generated);
      setStatementHistory((previous) => mergeAndSortHistory(previous, [generated]));
      setStatus(`Generated statement for ${generated.accountId} (${generated.month}).`);
    } catch (err) {
      handleStatementError(err, "generate");
    } finally {
      setIsGenerating(false);
    }
  }

  async function handleLoad(event: FormEvent) {
    event.preventDefault();
    setStatus("");
    setError("");

    if (!hasAccessToken) {
      setError("Your session is missing an access token. Please sign in again.");
      return;
    }

    if (!accountId.trim()) {
      setError("Provide an account ID before retrieving statements.");
      return;
    }

    setIsLoading(true);
    try {
      const loadedHistory = await listGeneratedMonthlyStatements(accessToken, accountId.trim(), month.trim());
      const normalizedHistory = mergeAndSortHistory([], loadedHistory);
      setStatementHistory(normalizedHistory);
      setStatement(normalizedHistory[0] ?? null);

      if (normalizedHistory.length === 0) {
        setStatus(`No generated statements found for account ${accountId.trim()}.`);
      } else {
        setStatus(`Loaded ${normalizedHistory.length} generated monthly statements for account ${accountId.trim()}.`);
      }
    } catch (err) {
      handleStatementError(err, "retrieve");
    } finally {
      setIsLoading(false);
    }
  }

  function handleStatementError(err: unknown, action: StatementAction) {
    const mapped = mapStatementApiError(err);
    const actionLabel = action === "generate" ? "generate" : "retrieve";
    setError(`Unable to ${actionLabel} monthly statement: ${mapped.message}`);

    if (isSessionRecoveryRequired(mapped.code)) {
      signOut();
    }
  }

  async function handleDownloadPdf() {
    if (!statement) {
      setError("Generate or retrieve a statement before downloading PDF.");
      return;
    }

    setError("");
    setStatus("");
    setIsDownloadingPdf(true);

    try {
      const pdfBytes = await buildStatementPdf(statement);
      const pdfArrayBuffer = Uint8Array.from(pdfBytes).buffer;
      const blob = new Blob([pdfArrayBuffer], { type: "application/pdf" });
      const objectUrl = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = objectUrl;
      anchor.download = `statement-${sanitizeForFileName(statement.accountId)}-${statement.month}.pdf`;
      document.body.appendChild(anchor);
      anchor.click();
      document.body.removeChild(anchor);
      URL.revokeObjectURL(objectUrl);

      setStatus(`Downloaded statement PDF for ${statement.accountId} (${statement.month}).`);
    } catch {
      setError("Unable to generate statement PDF. Please try again.");
    } finally {
      setIsDownloadingPdf(false);
    }
  }

  return (
    <section className="space-y-6">
      <header className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-6 shadow-soft backdrop-blur-xl">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-200/80">Statements</p>
        <h2 className="mt-2 text-3xl font-semibold text-white">Monthly statements</h2>
        <p className="mt-2 text-sm leading-6 text-slate-300">
          Generate statements by month and retrieve full generated statement history.
        </p>
      </header>

      <article className="animate-fade-up rounded-2xl border border-white/15 bg-slate-900/60 p-5 shadow-soft backdrop-blur-xl">
        <h3 className="text-xl font-semibold text-white">Generate or retrieve</h3>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <input
            type="text"
            value={accountId}
            onChange={(event) => setAccountId(event.target.value)}
            placeholder="Account ID"
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 placeholder:text-slate-400 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
          <input
            type="month"
            value={month}
            onChange={(event) => setMonth(event.target.value)}
            className="rounded-xl border border-white/20 bg-slate-950/70 px-4 py-3 text-sm text-slate-100 focus:border-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-400/40"
          />
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <form onSubmit={handleGenerate}>
            <button
              type="submit"
              disabled={!hasAccessToken || isGenerating || isLoading || isDownloadingPdf}
              className="inline-flex w-full items-center justify-center rounded-xl bg-cyan-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:bg-cyan-200/70"
            >
              {isGenerating ? "Generating..." : "Generate statement"}
            </button>
          </form>

          <form onSubmit={handleLoad}>
            <button
              type="submit"
              disabled={!hasAccessToken || isGenerating || isLoading || isDownloadingPdf}
              className="inline-flex w-full items-center justify-center rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/20 disabled:cursor-not-allowed disabled:opacity-70"
            >
              {isLoading ? "Loading..." : "Retrieve statements"}
            </button>
          </form>
        </div>

        {statementHistory.length > 0 ? (
          <div className="mt-6 overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
            <table className="min-w-full text-left text-xs text-slate-200">
              <thead className="border-b border-white/10 text-slate-400">
                <tr>
                  <th className="px-3 py-2 font-semibold">Month</th>
                  <th className="px-3 py-2 font-semibold">Generated</th>
                  <th className="px-3 py-2 font-semibold">Opening</th>
                  <th className="px-3 py-2 font-semibold">Closing</th>
                  <th className="px-3 py-2 font-semibold">Lines</th>
                  <th className="px-3 py-2 font-semibold">Action</th>
                </tr>
              </thead>
              <tbody>
                {statementHistory.map((item) => {
                  const isSelected = statement?.month === item.month && statement?.generatedAt === item.generatedAt;
                  return (
                    <tr
                      key={`${item.month}-${item.generatedAt}`}
                      className={[
                        "border-b border-white/5",
                        isSelected ? "bg-cyan-300/10" : "",
                      ].join(" ")}
                    >
                      <td className="px-3 py-2 font-semibold text-white">{item.month}</td>
                      <td className="px-3 py-2">{formatTimestamp(item.generatedAt)}</td>
                      <td className="px-3 py-2">${formatMoney(item.openingBalance)}</td>
                      <td className="px-3 py-2">${formatMoney(item.closingBalance)}</td>
                      <td className="px-3 py-2">{item.lineItems.length}</td>
                      <td className="px-3 py-2">
                        <button
                          type="button"
                          onClick={() => {
                            setStatement(item);
                            setStatus(`Viewing statement for ${item.accountId} (${item.month}) generated ${formatTimestamp(item.generatedAt)}.`);
                            setError("");
                          }}
                          className={[
                            "rounded-full px-3 py-1.5 text-xs font-semibold transition",
                            isSelected
                              ? "bg-cyan-300 text-slate-950"
                              : "bg-white/10 text-white hover:bg-white/20",
                          ].join(" ")}
                          disabled={isSelected}
                        >
                          {isSelected ? "Viewing" : "View"}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : null}

        {statement ? (
          <button
            type="button"
            onClick={() => {
              void handleDownloadPdf();
            }}
            disabled={isGenerating || isLoading || isDownloadingPdf}
            className="mt-3 inline-flex w-full items-center justify-center rounded-xl bg-indigo-300 px-4 py-3 text-sm font-semibold text-slate-950 transition hover:bg-indigo-200 disabled:cursor-not-allowed disabled:bg-indigo-200/70"
          >
            {isDownloadingPdf ? "Preparing PDF..." : "Download PDF"}
          </button>
        ) : null}

        {statement ? (
          <div className="mt-6 space-y-4">
            <div className="grid gap-3 rounded-xl border border-white/10 bg-slate-950/60 p-4 text-sm text-slate-200 sm:grid-cols-2">
              <p>
                <span className="text-slate-400">Account:</span> {statement.accountId}
              </p>
              <p>
                <span className="text-slate-400">Month:</span> {statement.month}
              </p>
              <p>
                <span className="text-slate-400">Opening balance:</span> ${formatMoney(statement.openingBalance)}
              </p>
              <p>
                <span className="text-slate-400">Closing balance:</span> ${formatMoney(statement.closingBalance)}
              </p>
              <p>
                <span className="text-slate-400">Total debits:</span> ${formatMoney(statement.totalDebits)}
              </p>
              <p>
                <span className="text-slate-400">Total credits:</span> ${formatMoney(statement.totalCredits)}
              </p>
            </div>

            {statement.lineItems.length > 0 ? (
              <div className="overflow-x-auto rounded-xl border border-white/10 bg-slate-950/60">
                <table className="min-w-full text-left text-xs text-slate-200">
                  <thead className="border-b border-white/10 text-slate-400">
                    <tr>
                      <th className="px-3 py-2 font-semibold">Posted</th>
                      <th className="px-3 py-2 font-semibold">Entry</th>
                      <th className="px-3 py-2 font-semibold">Amount</th>
                      <th className="px-3 py-2 font-semibold">Balance</th>
                      <th className="px-3 py-2 font-semibold">Description</th>
                    </tr>
                  </thead>
                  <tbody>
                    {statement.lineItems.map((item) => (
                      <tr key={`${item.transactionId}-${item.postedAt}`} className="border-b border-white/5">
                        <td className="px-3 py-2">{formatTimestamp(item.postedAt)}</td>
                        <td className="px-3 py-2">{item.entryType}</td>
                        <td className="px-3 py-2">${formatMoney(item.amount)}</td>
                        <td className="px-3 py-2">${formatMoney(item.balanceAfter)}</td>
                        <td className="px-3 py-2 text-slate-300">{item.description}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="rounded-xl border border-white/10 bg-slate-950/50 px-4 py-3 text-sm text-slate-300">
                This month has no posted activity. Totals remain valid with zeroed line items.
              </p>
            )}
          </div>
        ) : null}
      </article>

      {status ? (
        <p role="status" className="rounded-xl border border-emerald-300/30 bg-emerald-400/10 px-4 py-3 text-sm text-emerald-100">
          {status}
        </p>
      ) : null}

      {error ? (
        <p role="alert" className="rounded-xl border border-rose-300/30 bg-rose-400/10 px-4 py-3 text-sm text-rose-100">
          {error}
        </p>
      ) : null}
    </section>
  );
}

function mergeAndSortHistory(
  existing: MonthlyStatement[],
  incoming: MonthlyStatement[],
): MonthlyStatement[] {
  const latestByMonth = new Map<string, MonthlyStatement>();

  for (const statement of [...existing, ...incoming]) {
    const current = latestByMonth.get(statement.month);
    if (!current) {
      latestByMonth.set(statement.month, statement);
      continue;
    }

    const currentTime = new Date(current.generatedAt).getTime();
    const candidateTime = new Date(statement.generatedAt).getTime();
    if (Number.isFinite(candidateTime) && candidateTime >= currentTime) {
      latestByMonth.set(statement.month, statement);
    }
  }

  return Array.from(latestByMonth.values()).sort((left, right) => {
    const monthOrder = right.month.localeCompare(left.month);
    if (monthOrder !== 0) {
      return monthOrder;
    }

    const rightTime = new Date(right.generatedAt).getTime();
    const leftTime = new Date(left.generatedAt).getTime();
    return rightTime - leftTime;
  });
}

function formatTimestamp(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString();
}

function formatMoney(value: string): string {
  const parsed = Number.parseFloat(value);
  if (Number.isNaN(parsed) || !Number.isFinite(parsed)) {
    return "0.00";
  }
  return parsed.toFixed(2);
}

async function buildStatementPdf(statement: MonthlyStatement): Promise<Uint8Array> {
  const { PDFDocument, StandardFonts, rgb } = await import("pdf-lib");
  const pdfDoc = await PDFDocument.create();
  const regular = await pdfDoc.embedFont(StandardFonts.Helvetica);
  const bold = await pdfDoc.embedFont(StandardFonts.HelveticaBold);

  const pageWidth = 612;
  const pageHeight = 792;
  const margin = 42;
  const contentWidth = pageWidth - margin * 2;
  const sectionSpacing = 18;
  const textColor = rgb(0.11, 0.16, 0.24);
  const mutedTextColor = rgb(0.35, 0.43, 0.55);
  const borderColor = rgb(0.81, 0.85, 0.9);
  const sectionBackground = rgb(0.97, 0.98, 1);
  const headerBackground = rgb(0.88, 0.95, 0.99);

  let page = pdfDoc.addPage([pageWidth, pageHeight]);
  let cursorY = pageHeight - margin;

  function truncateToWidth(text: string, maxWidth: number, size: number, font = regular): string {
    const raw = text.trim();
    if (!raw) {
      return "-";
    }
    if (font.widthOfTextAtSize(raw, size) <= maxWidth) {
      return raw;
    }

    const ellipsis = "...";
    const ellipsisWidth = font.widthOfTextAtSize(ellipsis, size);
    let candidate = raw;

    while (candidate.length > 0 && font.widthOfTextAtSize(candidate, size) + ellipsisWidth > maxWidth) {
      candidate = candidate.slice(0, -1);
    }

    return `${candidate}${ellipsis}`;
  }

  function wrapToWidth(text: string, maxWidth: number, size: number, font = regular): string[] {
    const words = text.trim().split(/\s+/).filter(Boolean);
    if (words.length === 0) {
      return ["-"];
    }

    const lines: string[] = [];
    let currentLine = "";

    for (const word of words) {
      const candidate = currentLine ? `${currentLine} ${word}` : word;
      if (font.widthOfTextAtSize(candidate, size) <= maxWidth) {
        currentLine = candidate;
      } else if (!currentLine) {
        lines.push(truncateToWidth(word, maxWidth, size, font));
      } else {
        lines.push(currentLine);
        currentLine = word;
      }
    }

    if (currentLine) {
      lines.push(currentLine);
    }

    return lines;
  }

  function ensureSpace(requiredHeight: number) {
    if (cursorY - requiredHeight < margin) {
      page = pdfDoc.addPage([pageWidth, pageHeight]);
      cursorY = pageHeight - margin;
      drawDocumentHeader(true);
    }
  }

  function drawDocumentHeader(isContinuation = false) {
    const headerHeight = isContinuation ? 54 : 84;
    const y = cursorY - headerHeight;

    page.drawRectangle({
      x: margin,
      y,
      width: contentWidth,
      height: headerHeight,
      color: headerBackground,
      borderColor,
      borderWidth: 1,
    });

    page.drawText("Digital Bank", {
      x: margin + 14,
      y: y + headerHeight - 20,
      size: 10,
      font: bold,
      color: rgb(0.03, 0.35, 0.5),
    });

    page.drawText(isContinuation ? "Monthly Statement (continued)" : "Monthly Statement", {
      x: margin + 14,
      y: y + headerHeight - 38,
      size: isContinuation ? 13 : 18,
      font: bold,
      color: textColor,
    });

    if (!isContinuation) {
      page.drawText(`Account ${statement.accountId}  |  Month ${statement.month}`, {
        x: margin + 14,
        y: y + 12,
        size: 9.5,
        font: regular,
        color: mutedTextColor,
      });
    }

    cursorY -= headerHeight + sectionSpacing;
  }

  function drawSectionTitle(title: string) {
    const titleHeight = 20;
    ensureSpace(titleHeight + 6);

    page.drawText(title, {
      x: margin,
      y: cursorY - 14,
      size: 12,
      font: bold,
      color: textColor,
    });

    page.drawLine({
      start: { x: margin, y: cursorY - 18 },
      end: { x: margin + contentWidth, y: cursorY - 18 },
      thickness: 1,
      color: borderColor,
    });

    cursorY -= titleHeight;
  }

  function drawDetailCard(
    entries: Array<{ label: string; value: string }>,
    columns: number,
  ) {
    const cardPadding = 12;
    const rowHeight = 36;
    const rowCount = Math.ceil(entries.length / columns);
    const cardHeight = cardPadding * 2 + rowCount * rowHeight;
    ensureSpace(cardHeight);

    const cardY = cursorY - cardHeight;
    page.drawRectangle({
      x: margin,
      y: cardY,
      width: contentWidth,
      height: cardHeight,
      color: sectionBackground,
      borderColor,
      borderWidth: 1,
    });

    const innerWidth = contentWidth - cardPadding * 2;
    const columnWidth = innerWidth / columns;

    entries.forEach((entry, index) => {
      const row = Math.floor(index / columns);
      const col = index % columns;
      const x = margin + cardPadding + col * columnWidth;
      const baseY = cursorY - cardPadding - row * rowHeight;

      page.drawText(entry.label, {
        x,
        y: baseY - 10,
        size: 8,
        font: bold,
        color: mutedTextColor,
      });

      page.drawText(truncateToWidth(entry.value, columnWidth - 8, 10), {
        x,
        y: baseY - 24,
        size: 10,
        font: regular,
        color: textColor,
      });
    });

    cursorY -= cardHeight + sectionSpacing;
  }

  function drawRightAlignedCellText(text: string, rightEdge: number, y: number, size = 9.5) {
    const clipped = truncateToWidth(text, 160, size);
    const width = regular.widthOfTextAtSize(clipped, size);
    page.drawText(clipped, {
      x: Math.max(margin, rightEdge - width),
      y,
      size,
      font: regular,
      color: textColor,
    });
  }

  function toPdfTimestamp(value: string): string {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }

    const yyyy = parsed.getFullYear();
    const mm = String(parsed.getMonth() + 1).padStart(2, "0");
    const dd = String(parsed.getDate()).padStart(2, "0");
    const hh = String(parsed.getHours()).padStart(2, "0");
    const min = String(parsed.getMinutes()).padStart(2, "0");

    return `${yyyy}-${mm}-${dd} ${hh}:${min}`;
  }

  drawDocumentHeader(false);

  drawSectionTitle("Statement details");
  drawDetailCard(
    [
      { label: "Account", value: statement.accountId },
      { label: "Customer", value: statement.customerId },
      { label: "Month", value: statement.month },
      { label: "Timezone", value: statement.timezone },
      { label: "Generated", value: formatTimestamp(statement.generatedAt) },
    ],
    2,
  );

  drawSectionTitle("Summary");
  drawDetailCard(
    [
      { label: "Opening balance", value: `$${formatMoney(statement.openingBalance)}` },
      { label: "Closing balance", value: `$${formatMoney(statement.closingBalance)}` },
      { label: "Total debits", value: `$${formatMoney(statement.totalDebits)}` },
      { label: "Total credits", value: `$${formatMoney(statement.totalCredits)}` },
    ],
    2,
  );

  drawSectionTitle(`Line items (${statement.lineItems.length})`);

  if (statement.lineItems.length === 0) {
    const emptyHeight = 54;
    ensureSpace(emptyHeight);
    page.drawRectangle({
      x: margin,
      y: cursorY - emptyHeight,
      width: contentWidth,
      height: emptyHeight,
      color: sectionBackground,
      borderColor,
      borderWidth: 1,
    });
    page.drawText("No posted activity for this month.", {
      x: margin + 12,
      y: cursorY - 32,
      size: 10,
      font: regular,
      color: textColor,
    });
    cursorY -= emptyHeight + sectionSpacing;
  } else {
    const tableColumns = [
      { label: "Posted", width: 114 },
      { label: "Type", width: 58 },
      { label: "Amount", width: 74 },
      { label: "Balance", width: 78 },
      { label: "Description", width: contentWidth - 114 - 58 - 74 - 78 },
    ];

    const tableHeaderHeight = 24;
    const rowTextSize = 9.5;
    const rowLineHeight = 11;

    function drawTableHeader() {
      ensureSpace(tableHeaderHeight + 4);

      page.drawRectangle({
        x: margin,
        y: cursorY - tableHeaderHeight,
        width: contentWidth,
        height: tableHeaderHeight,
        color: rgb(0.93, 0.96, 0.99),
        borderColor,
        borderWidth: 1,
      });

      let runningX = margin;
      tableColumns.forEach((column, index) => {
        if (index > 0) {
          page.drawLine({
            start: { x: runningX, y: cursorY - tableHeaderHeight },
            end: { x: runningX, y: cursorY },
            thickness: 1,
            color: borderColor,
          });
        }

        page.drawText(column.label, {
          x: runningX + 6,
          y: cursorY - 16,
          size: 9,
          font: bold,
          color: textColor,
        });

        runningX += column.width;
      });

      cursorY -= tableHeaderHeight;
    }

    drawTableHeader();

    for (const item of statement.lineItems) {
      const descriptionLines = wrapToWidth(item.description, tableColumns[4].width - 10, rowTextSize)
        .slice(0, 3)
        .map((line, index, arr) => {
          if (index === arr.length - 1 && arr.length === 3) {
            return truncateToWidth(line, tableColumns[4].width - 10, rowTextSize);
          }
          return line;
        });

      const rowHeight = Math.max(24, descriptionLines.length * rowLineHeight + 10);

      if (cursorY - rowHeight < margin) {
        page = pdfDoc.addPage([pageWidth, pageHeight]);
        cursorY = pageHeight - margin;
        drawDocumentHeader(true);
        drawSectionTitle(`Line items (${statement.lineItems.length}) - continued`);
        drawTableHeader();
      }

      const rowBottomY = cursorY - rowHeight;

      page.drawRectangle({
        x: margin,
        y: rowBottomY,
        width: contentWidth,
        height: rowHeight,
        color: rgb(1, 1, 1),
        borderColor,
        borderWidth: 1,
      });

      let runningX = margin;
      tableColumns.forEach((column, index) => {
        if (index > 0) {
          page.drawLine({
            start: { x: runningX, y: rowBottomY },
            end: { x: runningX, y: cursorY },
            thickness: 1,
            color: borderColor,
          });
        }
        runningX += column.width;
      });

      const baselineY = cursorY - 14;

      const postedText = truncateToWidth(toPdfTimestamp(item.postedAt), tableColumns[0].width - 10, rowTextSize);
      page.drawText(postedText, {
        x: margin + 6,
        y: baselineY,
        size: rowTextSize,
        font: regular,
        color: textColor,
      });

      page.drawText(item.entryType, {
        x: margin + tableColumns[0].width + 6,
        y: baselineY,
        size: rowTextSize,
        font: regular,
        color: textColor,
      });

      const amountRight = margin + tableColumns[0].width + tableColumns[1].width + tableColumns[2].width - 6;
      drawRightAlignedCellText(`$${formatMoney(item.amount)}`, amountRight, baselineY, rowTextSize);

      const balanceRight =
        margin + tableColumns[0].width + tableColumns[1].width + tableColumns[2].width + tableColumns[3].width - 6;
      drawRightAlignedCellText(`$${formatMoney(item.balanceAfter)}`, balanceRight, baselineY, rowTextSize);

      let descriptionY = baselineY;
      for (const line of descriptionLines) {
        page.drawText(line, {
          x: margin + tableColumns[0].width + tableColumns[1].width + tableColumns[2].width + tableColumns[3].width + 6,
          y: descriptionY,
          size: rowTextSize,
          font: regular,
          color: textColor,
        });
        descriptionY -= rowLineHeight;
      }

      cursorY -= rowHeight;
    }
  }

  return pdfDoc.save();
}

function sanitizeForFileName(value: string): string {
  const normalized = value.trim().replace(/[^a-zA-Z0-9-_]+/g, "-");
  return normalized || "statement";
}
