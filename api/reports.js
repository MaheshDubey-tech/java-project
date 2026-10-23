const { calculateReports, setCors } = require('./data');

module.exports = (req, res) => {
  setCors(res);
  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  const url = new URL(req.url, 'http://localhost');
  const subject = (req.query?.subject || url.searchParams.get('subject') || 'ALL').toUpperCase();

  const reports = calculateReports(subject);
  return res.status(200).json(reports);
};
